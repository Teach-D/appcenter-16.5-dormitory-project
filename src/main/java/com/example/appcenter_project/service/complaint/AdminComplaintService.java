package com.example.appcenter_project.service.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintReplyDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintStatusDto;
import com.example.appcenter_project.dto.response.complaint.*;
import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.file.AttachedFileRepository;
import com.example.appcenter_project.repository.complaint.ComplaintReplyRepository;
import com.example.appcenter_project.repository.complaint.ComplaintRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.image.ImageService;
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

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final AttachedFileRepository attachedFileRepository;
    private final ImageService imageService;

    // 민원 답변 등록
    public ResponseComplaintReplyDto addReply(Long adminId, Long complaintId, RequestComplaintReplyDto dto, List<MultipartFile> images) {
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

        imageService.saveImages(ImageType.COMPLAINT_REPLY, reply.getId(), images);

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

    public void updateReply(Long userId, Long complaintId, RequestComplaintReplyDto dto, List<MultipartFile> images) {
        Complaint complaint = complaintRepository.findById(complaintId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));
        ComplaintReply reply = complaint.getReply();
        
        if (reply == null) {
            throw new CustomException(COMPLAINT_REPLY_NOT_FOUND);
        }

        Long replyId = reply.getId();
        
        // 답변 내용 업데이트
        reply.update(dto);

        imageService.updateImages(ImageType.COMPLAINT_REPLY, replyId, images);
    }

    @Transactional
    public void deleteReply(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));
        ComplaintReply reply = complaint.getReply();

        imageService.deleteImages(ImageType.COMPLAINT_REPLY, reply.getId());
        replyRepository.delete(reply);

        log.info("[deleteReply] 민원 답변 삭제 완료 - complaintId={}", complaintId);
    }

    public void updateComplaintOfficer(Long complaintId, String officer) {
        Complaint complaint = complaintRepository.findById(complaintId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));
        complaint.updateOfficer(officer);
    }
}
