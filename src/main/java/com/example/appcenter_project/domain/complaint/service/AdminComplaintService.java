package com.example.appcenter_project.domain.complaint.service;

import com.example.appcenter_project.domain.complaint.dto.request.RequestComplaintReplyDto;
import com.example.appcenter_project.domain.complaint.dto.request.RequestComplaintStatusDto;
import com.example.appcenter_project.domain.complaint.dto.response.ResponseComplaintReplyDto;
import com.example.appcenter_project.domain.complaint.entity.Complaint;
import com.example.appcenter_project.domain.complaint.entity.ComplaintReply;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.complaint.enums.ComplaintStatus;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.common.file.repository.AttachedFileRepository;
import com.example.appcenter_project.domain.complaint.repository.ComplaintReplyRepository;
import com.example.appcenter_project.domain.complaint.repository.ComplaintRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.notification.service.ComplaintNotificationService;
import com.example.appcenter_project.domain.notification.service.AdminComplaintNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

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
    private final ComplaintNotificationService complaintNotificationService;
    private final AdminComplaintNotificationService adminComplaintNotificationService;

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

        try {
            adminComplaintNotificationService.sendAndSaveComplaintReplyNotification(complaint);
            log.info("관리자 새 민원 알림 발송 완료 - 민원ID: {}", reply.getId());
        } catch (Exception e) {
            log.error("관리자 새 민원 알림 발송 실패 - 민원ID: {}", reply.getId(), e);
        }

/*        // 민원 답변 알림 발송
        try {
            complaintNotificationService.sendNewReplyNotification(complaintId);
            log.info("민원 답변 알림 발송 완료 - 민원ID: {}", complaintId);
        } catch (Exception e) {
            log.error("민원 답변 알림 발송 실패 - 민원ID: {}", complaintId, e);
        }*/

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

        ComplaintStatus oldStatus = complaint.getStatus();
        ComplaintStatus newStatus = ComplaintStatus.from(dto.getStatus());
        complaint.updateStatus(newStatus);

        adminComplaintNotificationService.sendAndSaveComplaintStatusNotification(complaint);


        /*// 상태가 실제로 변경된 경우에만 알림 발송
        if (!oldStatus.equals(newStatus)) {
            try {
                // 일반 유저에게 알림 발송
                complaintNotificationService.sendStatusChangeNotification(complaintId, newStatus);
                log.info("민원 상태 변경 알림 발송 완료 - 민원ID: {}, 상태: {} -> {}", 
                        complaintId, oldStatus.getDescription(), newStatus.getDescription());
                
                // 관리자에게 상태 변경 알림 발송
                adminComplaintNotificationService.sendStatusChangeNotification(complaintId, oldStatus, newStatus);
                log.info("관리자 민원 상태 변경 알림 발송 완료 - 민원ID: {}, 상태: {} -> {}", 
                        complaintId, oldStatus.getDescription(), newStatus.getDescription());
            } catch (Exception e) {
                log.error("민원 상태 변경 알림 발송 실패 - 민원ID: {}, 상태: {} -> {}", 
                        complaintId, oldStatus.getDescription(), newStatus.getDescription(), e);
            }
        }*/
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
