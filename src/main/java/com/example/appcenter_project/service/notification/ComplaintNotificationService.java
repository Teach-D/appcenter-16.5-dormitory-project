package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.complaint.ComplaintRepository;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.appcenter_project.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ComplaintNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final FcmMessageService fcmMessageService;

    /**
     * 민원 답변 시 알림 발송
     */
    public void sendNewReplyNotification(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        
        User complaintUser = complaint.getUser();
        String title = "생활원 민원";
        String body = "새로운 답변이 올라왔어요!\n민원 제목: " + complaint.getTitle();
        
        // 알림 생성
        Notification notification = Notification.builder()
                .boardId(complaintId)
                .title(title)
                .body(body)
                .notificationType(NotificationType.COMPLAINT)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // 사용자 알림 생성
        UserNotification userNotification = UserNotification.builder()
                .user(complaintUser)
                .notification(savedNotification)
                .isRead(false)
                .build();
        
        userNotificationRepository.save(userNotification);
        
        // FCM 푸시 알림 발송
        try {
            fcmMessageService.sendNotification(complaintUser, title, body);
            log.info("민원 답변 알림 발송 완료 - 사용자: {}, 민원ID: {}", complaintUser.getId(), complaintId);
        } catch (Exception e) {
            log.error("민원 답변 알림 발송 실패 - 사용자: {}, 민원ID: {}", complaintUser.getId(), complaintId, e);
        }
    }

    /**
     * 민원 상태 변경 시 알림 발송
     */
    public void sendStatusChangeNotification(Long complaintId, ComplaintStatus newStatus) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        
        User complaintUser = complaint.getUser();
        String title = "생활원 민원";
        String body = "접수한 민원의 상태가 " + newStatus.getDescription() + "으로 변경 되었어요!\n민원 제목: " + complaint.getTitle();
        
        // 알림 생성
        Notification notification = Notification.builder()
                .boardId(complaintId)
                .title(title)
                .body(body)
                .notificationType(NotificationType.COMPLAINT)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // 사용자 알림 생성
        UserNotification userNotification = UserNotification.builder()
                .user(complaintUser)
                .notification(savedNotification)
                .isRead(false)
                .build();
        
        userNotificationRepository.save(userNotification);
        
        // FCM 푸시 알림 발송
        try {
            fcmMessageService.sendNotification(complaintUser, title, body);
            log.info("민원 상태 변경 알림 발송 완료 - 사용자: {}, 민원ID: {}, 상태: {}", 
                    complaintUser.getId(), complaintId, newStatus.getDescription());
        } catch (Exception e) {
            log.error("민원 상태 변경 알림 발송 실패 - 사용자: {}, 민원ID: {}, 상태: {}", 
                    complaintUser.getId(), complaintId, newStatus.getDescription(), e);
        }
    }
}
