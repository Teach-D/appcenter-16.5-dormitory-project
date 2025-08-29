package com.example.appcenter_project.service.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintReplyDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintStatusDto;
import com.example.appcenter_project.dto.response.complaint.*;
import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.announcement.AttachedFile;
import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.announcement.AttachedFileRepository;
import com.example.appcenter_project.repository.complaint.ComplaintReplyRepository;
import com.example.appcenter_project.repository.complaint.ComplaintRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.appcenter_project.exception.ErrorCode.*;
import static com.google.common.io.Files.getFileExtension;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final AttachedFileRepository attachedFileRepository;

    // 민원 답변 등록
    public ResponseComplaintReplyDto addReply(Long adminId, Long complaintId, RequestComplaintReplyDto dto, List<MultipartFile> files) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));

        if (complaint.getReply() != null) {
            throw new CustomException(COMPLAINT_ALREADY_REPLIED);
        }

        ComplaintReply reply = ComplaintReply.builder()
                .replyTitle(dto.getReplyTitle())
                .replyContent(dto.getReplyContent())
                .responderName(dto.getResponderName())
                .complaint(complaint)
                .responder(admin)
                .build();

        complaint.addReply(reply);
        replyRepository.save(reply);

        complaint.updateStatus(ComplaintStatus.COMPLETED);

        // 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            saveUploadFile(reply, files);
        }


        return ResponseComplaintReplyDto.builder()
                .replyTitle(reply.getReplyTitle())
                .replyContent(reply.getReplyContent())
                .responderName(reply.getResponderName())
                .createdDate(reply.getCreatedDate().toString())
                .build();
    }

    // 민원 상태 변경
    public void updateStatus(Long complaintId, RequestComplaintStatusDto dto) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));

        ComplaintStatus status = ComplaintStatus.from(dto.getStatus());
        complaint.updateStatus(status);
    }

    public void updateReply(Long userId, Long complaintId, RequestComplaintReplyDto dto, List<MultipartFile> files) {
        Complaint complaint = complaintRepository.findById(complaintId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));
        ComplaintReply reply = complaint.getReply();
        
        if (reply == null) {
            throw new CustomException(COMPLAINT_REPLY_NOT_FOUND);
        }

        Long replyId = reply.getId();
        
        // 기존 첨부파일 삭제 (물리적 파일 + DB 레코드)
        deletePhysicalFilesAndDbRecords(replyId);
        
        // 답변 내용 업데이트
        reply.update(dto);

        // 새로운 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            saveUploadFile(reply, files);
        }
    }

    @Transactional
    public void deleteReply(Long complaintId) {

        replyRepository.deleteByComplaintId(complaintId);
        
        log.info("[deleteReply] 민원 답변 삭제 완료 - complaintId={}", complaintId);
    }

    private void deleteAttachedFilesCompletely(Long replyId) {
        log.info("[deleteAttachedFilesCompletely] 첨부파일 완전 삭제 시작 - replyId={}", replyId);

        List<AttachedFile> attachedFiles = attachedFileRepository.findByComplaintReplyId(replyId);
        log.info("[deleteAttachedFilesCompletely] 발견된 첨부파일 개수: {}", attachedFiles.size());

        // 1. 물리적 파일 삭제
        for (AttachedFile attachedFile : attachedFiles) {
            File file = new File(attachedFile.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("물리적 파일 삭제 성공: {}", attachedFile.getFilePath());
                } else {
                    log.warn("물리적 파일 삭제 실패: {}", attachedFile.getFilePath());
                }
            } else {
                log.warn("삭제할 물리적 파일이 존재하지 않음: {}", attachedFile.getFilePath());
            }
        }

        // 2. DB 레코드 삭제 (complaintReplyId로 일괄 삭제)
        int deletedCount = attachedFileRepository.deleteByComplaintReplyId(replyId);
        log.info("[deleteAttachedFilesCompletely] DB에서 삭제된 첨부파일 레코드 수: {}", deletedCount);
        
        log.info("[deleteAttachedFilesCompletely] 첨부파일 완전 삭제 완료 - replyId={}", replyId);
    }

    private void deletePhysicalFilesByReplyId(Long replyId) {
        log.info("[deletePhysicalFilesByReplyId] 물리적 파일 삭제 시작 - replyId={}", replyId);

        List<AttachedFile> existingFiles = attachedFileRepository.findByComplaintReplyId(replyId);

        for (AttachedFile attachedFile : existingFiles) {
            // 파일시스템에서만 파일 삭제 (DB 레코드는 CASCADE로 자동 삭제됨)
            File file = new File(attachedFile.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("파일 삭제 성공: {}", attachedFile.getFilePath());
                } else {
                    log.warn("파일 삭제 실패: {}", attachedFile.getFilePath());
                }
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", attachedFile.getFilePath());
            }
        }

        log.info("[deletePhysicalFilesByReplyId] 물리적 파일 삭제 완료 - 처리된 파일 수: {}", existingFiles.size());
    }

    private void deletePhysicalFilesAndDbRecords(Long replyId) {
        log.info("[deletePhysicalFilesAndDbRecords] 첨부파일 삭제 시작 - complaintReply={}", replyId);

        List<AttachedFile> existingFiles = attachedFileRepository.findByComplaintReplyId(replyId);

        for (AttachedFile attachedFile : existingFiles) {
            // 파일시스템에서 파일 삭제
            File file = new File(attachedFile.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("파일 삭제 성공: {}", attachedFile.getFilePath());
                } else {
                    log.warn("파일 삭제 실패: {}", attachedFile.getFilePath());
                }
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", attachedFile.getFilePath());
            }
        }

        // DB에서 첨부파일 레코드 삭제
        attachedFileRepository.deleteByComplaintReplyId(replyId);

        log.info("[deletePhysicalFilesAndDbRecords] 첨부파일 삭제 완료 - 삭제된 파일 수: {}", existingFiles.size());
    }

    private void saveUploadFile(ComplaintReply complaintReply, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // 개발 환경에 맞는 경로 설정
            String basePath = System.getProperty("user.dir");
            String filePath = basePath + "/files/complaint_reply/";

            // 디렉토리 생성 (존재하지 않으면)
            File directory = new File(filePath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
            }

            // 첨부파일 저장
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    log.warn("Empty file skipped during complaint_reply image save");
                    continue;
                }

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString();
                String uploadFileName = "complaint_reply" + complaintReply.getId() + "_" + uuid + timestamp + fileExtension;
                File destinationFile = new File(filePath + uploadFileName);

                try {
                    file.transferTo(destinationFile);
                    log.info("ComplaintReply file saved successfully: {}", destinationFile.getAbsolutePath());

                    AttachedFile attachedFile = AttachedFile.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .fileName(file.getOriginalFilename())
                            .fileSize(file.getSize())
                            .complaintReply(complaintReply)
                            .build();

                    attachedFileRepository.save(attachedFile);

                    complaintReply.getAttachedFiles().add(attachedFile);

                } catch (IOException e) {
                    log.error("Failed to save ComplaintReply file for complaintReply {}: ", complaintReply.getId(), e);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }
        }
    }

    // 파일 확장자 추출 헬퍼 메소드
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".pdf"; // 기본 확장자
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".pdf"; // 확장자가 없으면 기본값
        }

        return fileName.substring(lastDotIndex).toLowerCase();
    }

    public void updateComplaintOfficer(Long complaintId, String officer) {
        Complaint complaint = complaintRepository.findById(complaintId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));
        complaint.updateOfficer(officer);
    }
}
