package com.example.appcenter_project.service.complaint;

import com.example.appcenter_project.dto.AttachedFileDto;
import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintSearchDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDetailDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintReplyDto;
import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.complaint.DormBuilding;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.file.AttachedFileRepository;
import com.example.appcenter_project.repository.complaint.ComplaintRepository;
import com.example.appcenter_project.repository.complaint.ComplaintSpecification;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.notification.AdminComplaintNotificationService;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.appcenter_project.service.image.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static com.example.appcenter_project.exception.ErrorCode.*;
import static com.example.appcenter_project.exception.ErrorCode.COMPLAINT_NOT_OWNED_BY_USER;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final AttachedFileRepository attachedFileRepository;
    private final AdminComplaintNotificationService adminComplaintNotificationService;
    private final ImageService imageService;

    // 민원 등록
    public ResponseComplaintDto createComplaint(Long userId, RequestComplaintDto dto, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 필수 항목 검증
        if (dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getContent() == null || dto.getContent().isBlank()
                || dto.getDormType() == null || dto.getDormType().isBlank()
                || dto.getBuilding() == null || dto.getBuilding().isBlank()) {
            throw new CustomException(COMPLAINT_REQUIRED_FIELD_MISSING);
        }

        ComplaintType type = ComplaintType.from(dto.getType());
        DormType dormType = DormType.from(dto.getDormType());
        DormBuilding building = DormBuilding.from(dto.getBuilding());

        Complaint complaint = Complaint.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .dormType(dormType)
                .type(type)
                .user(user)
                .building(building)
                .floor(dto.getFloor())
                .roomNumber(dto.getRoomNumber())
                .bedNumber(dto.getBedNumber())
                .isPrivacyAgreed(dto.isPrivacyAgreed())
                .build();

        Complaint saved = complaintRepository.save(complaint);

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            imageService.saveImages(ImageType.COMPLAINT, complaint.getId(), images);
        }

        try {
            adminComplaintNotificationService.sendAndSaveComplaintNotification(complaint);
            log.info("관리자 새 민원 알림 발송 완료 - 민원ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("관리자 새 민원 알림 발송 실패 - 민원ID: {}", saved.getId(), e);
        }


/*        // 관리자에게 새 민원 접수 알림 발송
        try {
            adminComplaintNotificationService.sendNewComplaintNotification(saved.getId());
            log.info("관리자 새 민원 알림 발송 완료 - 민원ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("관리자 새 민원 알림 발송 실패 - 민원ID: {}", saved.getId(), e);
        }*/

        return ResponseComplaintDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .dormType(saved.getDormType().toValue())
                .type(saved.getType().toValue())
                .building(saved.getBuilding().toValue())
                .floor(saved.getFloor())
                .roomNumber(saved.getRoomNumber())
                .bedNumber(saved.getBedNumber())
                .isPrivacyAgreed(saved.isPrivacyAgreed())
                .status(saved.getStatus().toValue())
                .createdDate(saved.getCreatedDate().toString())
                .build();
    }

    // 민원 전체 조회 (최신순)
    public List<ResponseComplaintListDto> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(c -> ResponseComplaintListDto.builder()
                        .id(c.getId())
                        .date(c.getCreatedDate().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .type(c.getType().toValue())
                        .title(c.getTitle())
                        .status(c.getStatus().toValue())
                        .officer(c.getOfficer())
                        .dormType(c.getDormType().toValue())
                        .building(c.getBuilding() != null ? c.getBuilding().toValue() : null)
                        .floor(c.getFloor())
                        .roomNumber(c.getRoomNumber())
                        .bedNumber(c.getBedNumber())
                        .build())
                .collect(Collectors.toList());
    }

    // 민원 전체 조회 (최신순)
    public List<ResponseComplaintListDto> getAllComplaintsUserId(Long userId) {
        return complaintRepository.findAllByUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(c -> ResponseComplaintListDto.builder()
                        .id(c.getId())
                        .date(c.getCreatedDate()
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .type(c.getType().toValue())
                        .title(c.getTitle())
                        .status(c.getStatus().toValue())
                        .officer(c.getOfficer())
                        .dormType(c.getDormType() != null ? c.getDormType().toValue() : null)
                        .building(c.getBuilding() != null ? c.getBuilding().toValue() : null)
                        .floor(c.getFloor())
                        .roomNumber(c.getRoomNumber())
                        .bedNumber(c.getBedNumber())
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
            List<AttachedFile> attachedFiles = attachedFileRepository.findByComplaintReply(r);
            List<AttachedFileDto> attachedFileDtos = getAttachedFile(attachedFiles, request);

            replyDto = ResponseComplaintReplyDto.builder()
                    .replyTitle(r.getReplyTitle())
                    .replyContent(r.getReplyContent())
                    .responderName(r.getResponderName())
                    .attachmentUrl(attachedFileDtos)
                    .createdDate(r.getCreatedDate().toString())
                    .build();
        }


        // 이미지 조회
        List<String> complaintImages = imageService.findStaticImageUrls(ImageType.COMPLAINT, c.getId(), request);
        return ResponseComplaintDetailDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .type(c.getType().toValue())
                .dormType(c.getDormType() != null ? c.getDormType().toValue() : null)
                .building(c.getBuilding() != null ? c.getBuilding().toValue() : null)
                .floor(c.getFloor())
                .roomNumber(c.getRoomNumber())
                .bedNumber(c.getBedNumber())
                .status(c.getStatus().toValue())
                .createdDate(c.getCreatedDate().toString())
                .reply(replyDto)
                .officer(c.getOfficer())
                .images(complaintImages)
                .build();
    }

    // 관리자 상세조회
    public ResponseComplaintDetailDto getComplaintDetail(Long complaintId, HttpServletRequest request) {
        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(COMPLAINT_NOT_FOUND));

        ResponseComplaintReplyDto replyDto = null;
        ComplaintReply r = c.getReply();

        List<AttachedFile> attachedFiles = attachedFileRepository.findByComplaintReply(r);
        List<AttachedFileDto> attachedFileDtos = getAttachedFile(attachedFiles, request);

        if (r != null) {
            replyDto = ResponseComplaintReplyDto.builder()
                    .replyTitle(r.getReplyTitle())
                    .replyContent(r.getReplyContent())
                    .responderName(r.getResponderName())
                    .attachmentUrl(attachedFileDtos)
                    .createdDate(r.getCreatedDate().toString())
                    .build();
        }

        // 이미지 조회
        List<String> complaintImages = imageService.findImages(ImageType.COMPLAINT, c.getId(), request).stream()
                .map(ImageLinkDto::getImageUrl)
                .toList();

        return ResponseComplaintDetailDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .type(c.getType().toValue())
                .dormType(c.getDormType().toValue())
                .building(c.getBuilding().toValue())
                .floor(c.getFloor())
                .roomNumber(c.getRoomNumber())
                .bedNumber(c.getBedNumber())
                .status(c.getStatus().toValue())
                .createdDate(c.getCreatedDate().toString())
                .reply(replyDto)
                .images(complaintImages)
                .officer(c.getOfficer())
                .build();
    }

    private List<AttachedFileDto> getAttachedFile(List<AttachedFile> attachedFiles , HttpServletRequest request) {
        if (attachedFiles.isEmpty()) {
            return new ArrayList<>(); // 빈 리스트 반환
        }

        // BaseURL 생성
        String baseUrl = getBaseUrl(request);
        List<AttachedFileDto> attachedFileDtos = new ArrayList<>();

        for (AttachedFile attachedFile : attachedFiles) {
            File file = new File(attachedFile.getFilePath());
            if (file.exists()) {
                String fileUrl = baseUrl + "/api/files/complaint_reply/" + attachedFile.getId();

                // 정적 리소스 URL 생성 (User와 동일한 방식)
                String staticImageUrl = getStaticAttachedFileUrl(attachedFile.getFilePath(), baseUrl);
                String changeUrl = staticImageUrl.replace("http", "https");

                AttachedFileDto attachedFileDto = AttachedFileDto.builder()
                        .filePath(changeUrl)
                        .fileName(attachedFile.getFileName())
                        .fileSize(attachedFile.getFileSize())
                        .build();

                attachedFileDtos.add(attachedFileDto);
            } else {
                log.warn("AttachedFile not found: {}", attachedFile.getFilePath());
            }
        }


        return attachedFileDtos;
    }

    // 정적 첨부파일 이미지 URL 생성 헬퍼 메소드
    private String getStaticAttachedFileUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/files/complaint_reply/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for Attached file path: {}", filePath);
            return null;
        }
    }

    public void updateComplaint(Long userId, RequestComplaintDto dto, Long complaintId, List<MultipartFile> images) {
        Complaint complaint = complaintRepository.findByIdAndUserId(complaintId, userId).orElseThrow(() -> new CustomException(COMPLAINT_NOT_OWNED_BY_USER));
        complaint.update(dto);

        imageService.updateImages(ImageType.COMPLAINT, complaintId, images);
    }

    public void deleteComplaint(Long userId, Long complaintId) {
        complaintRepository.deleteById(complaintId);
        imageService.deleteImages(ImageType.COMPLAINT, complaintId);
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

    //필터 검색
    @Transactional(readOnly = true)
    public List<ResponseComplaintListDto> searchComplaints(Long userId, RequestComplaintSearchDto dto) {

        Specification<Complaint> spec = Specification
                .where(ComplaintSpecification.hasDormType(dto.getDormType()))
                .and(ComplaintSpecification.hasOfficer(dto.getOfficer()))
                .and(ComplaintSpecification.hasStatus(dto.getStatus()))
                .and(ComplaintSpecification.hasKeyword(dto.getKeyword()))
                .and(ComplaintSpecification.hasType(dto.getType()))
                .and(ComplaintSpecification.hasBuilding(
                        dto.getBuilding() != null && !dto.getBuilding().isBlank()
                                ? DormBuilding.valueOf(dto.getBuilding())
                                : null
                ))
                .and(ComplaintSpecification.hasFloor(dto.getFloor()))
                .and(ComplaintSpecification.hasRoomNumber(dto.getRoomNumber()))
                .and(ComplaintSpecification.hasBedNumber(dto.getBedNumber()));


        // 사용자일 경우 본인 것만
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        return complaintRepository.findAll(spec).stream()
                .map(c -> ResponseComplaintListDto.builder()
                        .id(c.getId())
                        .date(c.getCreatedDate() != null
                                ? c.getCreatedDate().toLocalDate().toString()
                                : null)
                        .type(c.getType() != null ? c.getType().toValue() : null)
                        .title(c.getTitle())
                        .status(c.getStatus() != null ? c.getStatus().toValue() : null)
                        .officer(c.getOfficer())
                        .dormType(c.getDormType() != null ? c.getDormType().toValue() : null)
                        .building(c.getBuilding() != null ? c.getBuilding().toValue() : null)
                        .floor(c.getFloor())
                        .roomNumber(c.getRoomNumber())
                        .bedNumber(c.getBedNumber())
                        .build())
                .toList();
    }


}
