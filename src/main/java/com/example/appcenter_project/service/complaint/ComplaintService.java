package com.example.appcenter_project.service.complaint;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDetailDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintReplyDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.complaint.ComplaintRepository;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.user.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.example.appcenter_project.exception.ErrorCode.*;
import static com.example.appcenter_project.exception.ErrorCode.COMPLAINT_NOT_OWNED_BY_USER;
import static com.google.common.io.Files.getFileExtension;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    // 민원 등록
    public ResponseComplaintDto createComplaint(Long userId, RequestComplaintDto dto, List<MultipartFile> files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getContent() == null || dto.getContent().isBlank()
                || dto.getCaseNumber() == null || dto.getCaseNumber().isBlank()
                || dto.getContact() == null || dto.getContact().isBlank()) {
            throw new CustomException(COMPLAINT_REQUIRED_FIELD_MISSING);
        }

        ComplaintType type = ComplaintType.from(dto.getType());
        DormType dormType = DormType.from(dto.getDormType());

        Complaint complaint = Complaint.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .caseNumber(dto.getCaseNumber())
                .contact(dto.getContact())
                .dormType(dormType)
                .type(type)
                .user(user)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        // 이미지 저장
        if (!files.isEmpty()) {
            saveImages(complaint, files);
        }

        return ResponseComplaintDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .caseNumber(saved.getCaseNumber())
                .contact(saved.getContact())
                .dormType(saved.getDormType().toValue())
                .type(saved.getType().toValue())
                .status(saved.getStatus().toValue())
                .createdDate(saved.getCreatedDate().toString())
                .build();
    }

    // 민원 전체 조회 (최신순)
    public List<ResponseComplaintListDto> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(c -> ResponseComplaintListDto.builder()
                        .date(c.getCreatedDate().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("MM.dd")))
                        .type(c.getType().toValue())
                        .title(c.getTitle())
                        .status(c.getStatus().toValue())
                        .build())
                .collect(Collectors.toList());
    }

    // 민원 전체 조회 (최신순)
    public List<ResponseComplaintListDto> getAllComplaintsUserId(Long userId) {
        return complaintRepository.findAllByUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(c -> ResponseComplaintListDto.builder()
                        .date(c.getCreatedDate().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("MM.dd")))
                        .type(c.getType().toValue())
                        .title(c.getTitle())
                        .status(c.getStatus().toValue())
                        .build())
                .collect(Collectors.toList());
    }

    //상세조회
    public ResponseComplaintDetailDto getComplaintDetailByUserId(Long userId, Long complaintId, HttpServletRequest request) {
        Complaint c = complaintRepository.findByIdAndUserId(complaintId, userId)
                .orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));

        ResponseComplaintReplyDto replyDto = null;
        ComplaintReply r = c.getReply();
        if (r != null) {
            replyDto = ResponseComplaintReplyDto.builder()
                    .replyTitle(r.getReplyTitle())
                    .replyContent(r.getReplyContent())
                    .responderName(r.getResponderName())
                    .attachmentUrl(r.getAttachmentUrl())
                    .createdDate(r.getCreatedDate().toString())
                    .build();
        }

        for (Image image : c.getImageList()) {
            log.info(image.getFilePath());
        }

        // 이미지 조회
        List<String> complaintImages = getComplaintImage(c.getImageList(), request);

        return ResponseComplaintDetailDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .type(c.getType().toValue())
                .dormType(c.getDormType().toValue())
                .caseNumber(c.getCaseNumber())
                .contact(c.getContact())
                .status(c.getStatus().toValue())
                .createdDate(c.getCreatedDate().toString())
                .reply(replyDto)
                .images(complaintImages)
                .build();
    }

    // 관리자 상세조회
    public ResponseComplaintDetailDto getComplaintDetail(Long complaintId, HttpServletRequest request) {
        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));

        ResponseComplaintReplyDto replyDto = null;
        ComplaintReply r = c.getReply();
        if (r != null) {
            replyDto = ResponseComplaintReplyDto.builder()
                    .replyTitle(r.getReplyTitle())
                    .replyContent(r.getReplyContent())
                    .responderName(r.getResponderName())
                    .attachmentUrl(r.getAttachmentUrl())
                    .createdDate(r.getCreatedDate().toString())
                    .build();
        }

        // 이미지 조회
        List<String> complaintImages = getComplaintImage(c.getImageList(), request);

        return ResponseComplaintDetailDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .type(c.getType().toValue())
                .dormType(c.getDormType().toValue())
                .caseNumber(c.getCaseNumber())
                .contact(c.getContact())
                .status(c.getStatus().toValue())
                .createdDate(c.getCreatedDate().toString())
                .reply(replyDto)
                .images(complaintImages)
                .build();
    }

    public void updateComplaint(Long userId, RequestComplaintDto dto, Long complaintId, List<MultipartFile> images) {
        Complaint complaint = complaintRepository.findByIdAndUserId(complaintId, userId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_OWNED_BY_USER));
        complaint.update(dto);

        // 이미지가 제공된 경우에만 기존 이미지를 삭제하고 새로운 이미지를 저장
        if (images != null && !images.isEmpty()) {
            // 기존 이미지들이 있다면 파일 및 DB에서 삭제
            List<Image> existingImages = complaint.getImageList();
            for (Image existingImage : existingImages) {
                File oldFile = new File(existingImage.getFilePath());
                if (oldFile.exists()) {
                    boolean deleted = oldFile.delete();
                    if (!deleted) {
                        log.warn("Failed to delete old complaint image file: {}", existingImage.getFilePath());
                    }
                }
                // 기존 이미지 엔티티 삭제
                imageRepository.delete(existingImage);
            }
            complaint.getImageList().clear(); // Tip에서 이미지 목록 비우기

            // 새로운 이미지들 저장
            saveImages(complaint, images);
            log.info("[updateComplaint] complaintId={}의 새로운 이미지 저장 완료", complaintId);
        }
    }

    public void deleteComplaint(Long userId, Long complaintId) {
        Complaint complaint = complaintRepository.findByIdAndUserId(complaintId, userId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_OWNED_BY_USER));
        complaintRepository.delete(complaint);

        List<Image> existingImages = complaint.getImageList();
        for (Image existingImage : existingImages) {
            File oldFile = new File(existingImage.getFilePath());
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete old complaint image file: {}", existingImage.getFilePath());
                }
            }
            // 기존 이미지 엔티티 삭제
            imageRepository.delete(existingImage);
        }
    }

    private void saveImages(Complaint complaint, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // 개발 환경에 맞는 경로 설정
            String basePath = System.getProperty("user.dir");
            String imagePath = basePath + "/images/complaint/";

            // 디렉토리 생성 (존재하지 않으면)
            File directory = new File(imagePath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    log.error("Failed to create complaint directory: {}", imagePath);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    log.warn("Empty file skipped during complaint image save");
                    continue;
                }

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString();
                String imageFileName = "complaint_" + complaint.getId() + "_" + uuid + fileExtension;
                File destinationFile = new File(imagePath + imageFileName);

                try {
                    file.transferTo(destinationFile);
                    log.info("complaint image saved successfully: {}", destinationFile.getAbsolutePath());

                    Image image = Image.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .isDefault(false)
                            .imageType(ImageType.COMPLAINT)
                            .boardId(complaint.getId())
                            .build();

                    imageRepository.save(image);
                    complaint.getImageList().add(image);

                } catch (IOException e) {
                    log.error("Failed to save complaint image file for complaint {}: ", complaint.getId(), e);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }
        }
    }

    public List<String> getComplaintImage(List<Image> images, HttpServletRequest request) {
        List<String> complaintFiles = new ArrayList<>();

        for (Image image : images) {
            // BaseURL 생성
            String baseUrl = getBaseUrl(request);
            File file = new File(image.getFilePath());
            if (file.exists()) {
                String imageUrl = baseUrl + "/api/images/complaint/" + image.getId();

                // 정적 리소스 URL 생성 (User와 동일한 방식)
                String staticImageUrl = getStaticComplaintImageUrl(image.getFilePath(), baseUrl);
                String changeUrl = staticImageUrl.replace("http", "https");
                complaintFiles.add(changeUrl);
            } else {
                log.warn("[getComplaintImage] 파일이 존재하지 않음 - path={}", image.getFilePath());
            }

        }

        return complaintFiles;
    }

    // User 방식과 동일한 파일 확장자 추출 메소드
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".jpg"; // 기본 확장자
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".jpg"; // 확장자가 없으면 기본값
        }

        return fileName.substring(lastDotIndex).toLowerCase();
    }

    // 유틸리티: 베이스 URL 생성 (ImageService와 동일)
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // 기본 포트가 아닌 경우에만 포트 추가
        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }

        baseUrl.append(contextPath);
        return baseUrl.toString();
    }

    // 정적 Complaint 이미지 URL 생성 헬퍼 메소드
    private String getStaticComplaintImageUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/images/complaint/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for complaint image path: {}", filePath);
            return null;
        }
    }

}
