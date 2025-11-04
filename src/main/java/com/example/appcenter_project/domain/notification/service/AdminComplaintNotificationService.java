package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.complaint.entity.Complaint;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.complaint.enums.ComplaintStatus;
import com.example.appcenter_project.domain.complaint.enums.ComplaintType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.complaint.repository.ComplaintRepository;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.shared.utils.WorkingHoursValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminComplaintNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;
    private final FcmMessageService fcmMessageService;
    private final WorkingHoursValidator workingHoursValidator;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 새 민원 접수 시 관리자에게 알림 발송
     */
    public void sendNewComplaintNotification(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        
        String title = "생활원 민원";
        String body = "새로운 민원이 접수되었습니다\n민원 제목: " + complaint.getTitle();
        
        // 주말/퇴근시간 체크
        if (shouldDelayNotification()) {
            scheduleDelayedNotification(complaintId, title, body, "NEW_COMPLAINT");
            return;
        }
        
        sendToAllAdmins(complaintId, title, body);
    }

    /**
     * 민원 상태 변경 시 관리자에게 알림 발송
     */
    public void sendStatusChangeNotification(Long complaintId, ComplaintStatus oldStatus, ComplaintStatus newStatus) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        
        String title = "생활원 민원";
        String body = getStatusChangeMessage(oldStatus, newStatus) + "\n민원 제목: " + complaint.getTitle();
        
        // 주말/퇴근시간 체크
        if (shouldDelayNotification()) {
            scheduleDelayedNotification(complaintId, title, body, "STATUS_CHANGE");
            return;
        }
        
        sendToAllAdmins(complaintId, title, body);
    }

    /**
     * 주말/퇴근시간 이후인지 체크
     */
    private boolean shouldDelayNotification() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        
        // 주말 체크 (토요일=6, 일요일=7)
        int dayOfWeek = now.getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return true;
        }
        
        // 퇴근시간 이후 체크 (오후 6시 이후)
        return currentTime.isAfter(LocalTime.of(18, 0));
    }

    /**
     * 지연된 알림 스케줄링
     */
    @Async
    public void scheduleDelayedNotification(Long complaintId, String title, String body, String type) {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();
        
        String scheduleTime;
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            // 주말인 경우: 다음 월요일 오전 9시
            scheduleTime = "다음 월요일 오전 9시";
        } else {
            // 평일 퇴근시간 이후: 다음날 오전 9시
            scheduleTime = "다음날 오전 9시";
        }
        
        // TODO: 실제 스케줄링 구현 (Quartz, @Scheduled 등 사용)
        // 현재는 로그만 출력
        log.info("민원 알림이 {}에 발송 예정 - 민원ID: {}, 타입: {}", scheduleTime, complaintId, type);
    }

    /**
     * 모든 관리자에게 알림 발송
     */
    private void sendToAllAdmins(Long complaintId, String title, String body) {
        List<User> admins = userRepository.findByRoleIn(Arrays.asList(Role.ROLE_ADMIN));
        
        if (admins.isEmpty()) {
            log.warn("관리자 사용자가 없습니다.");
            return;
        }
        
        // 알림 생성
        Notification notification = Notification.builder()
                .boardId(complaintId)
                .title(title)
                .body(body)
                .notificationType(NotificationType.COMPLAINT)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // 각 관리자에게 알림 발송
        for (User admin : admins) {
            // 사용자 알림 생성
            UserNotification userNotification = UserNotification.builder()
                    .user(admin)
                    .notification(savedNotification)
                    .isRead(false)
                    .build();
            
            userNotificationRepository.save(userNotification);
            
            // FCM 푸시 알림 발송
            try {
                fcmMessageService.sendNotification(admin, title, body);
                log.info("관리자 민원 알림 발송 완료 - 관리자: {}, 민원ID: {}", admin.getId(), complaintId);
            } catch (Exception e) {
                log.error("관리자 민원 알림 발송 실패 - 관리자: {}, 민원ID: {}", admin.getId(), complaintId, e);
            }
        }
    }

    /**
     * 상태 변경 메시지 생성
     */
    private String getStatusChangeMessage(ComplaintStatus oldStatus, ComplaintStatus newStatus) {
        if (oldStatus == ComplaintStatus.IN_PROGRESS && newStatus == ComplaintStatus.COMPLETED) {
            return "민원 처리가 완료되었습니다";
        } else {
            return "민원 상태가 " + oldStatus.getDescription() + "에서 " + newStatus.getDescription() + "으로 변경되었습니다";
        }
    }

    public void sendAndSaveComplaintNotification(Complaint complaint) {

        if (complaint.getType() == ComplaintType.ROOMMATE_CHANGE) {
            String title = "새로운 룸메이트 민원이 작성되었습니다!";

            Notification notification = Notification.builder()
                    .boardId(complaint.getId())
                    .title(title)
                    .body(complaint.getTitle())
                    .notificationType(NotificationType.COMPLAINT)
                    .apiType(ApiType.COMPLAINT)
                    .build();

            notificationRepository.save(notification);

            List<User> roommateComplaintManagers = userRepository.findByRole(Role.ROLE_DORM_ROOMMATE_MANAGER);
            for (User roommateComplaintManager : roommateComplaintManagers) {
                UserNotification userNotification = UserNotification.of(roommateComplaintManager, notification);
                userNotificationRepository.save(userNotification);

                if (workingHoursValidator.isNotWorkingHours()) {
                    redisTemplate.opsForValue().set("complaint_queue:user_notification:" + userNotification.getId(), userNotification.getId());
                } else {
                    fcmMessageService.sendNotification(roommateComplaintManager, title, notification.getBody());
                }

            }
        } else {
            String title = "새로운 생활 민원이 작성되었습니다!";

            Notification notification = Notification.builder()
                    .boardId(complaint.getId())
                    .title(title)
                    .body(complaint.getTitle())
                    .notificationType(NotificationType.COMPLAINT)
                    .apiType(ApiType.COMPLAINT)
                    .build();

            notificationRepository.save(notification);

            List<User> lifeComplaintManagers = userRepository.findByRole(Role.ROLE_DORM_LIFE_MANAGER);
            for (User lifeComplaintManager : lifeComplaintManagers) {
                UserNotification userNotification = UserNotification.of(lifeComplaintManager, notification);
                userNotificationRepository.save(userNotification);

                if (workingHoursValidator.isNotWorkingHours()) {
                    redisTemplate.opsForValue().set("complaint_queue:user_notification:" + userNotification.getId(), userNotification.getId());
                } else {
                    fcmMessageService.sendNotification(lifeComplaintManager, title, notification.getBody());
                }
            }
        }
    }

    public void sendAndSaveComplaintReplyNotification(Complaint complaint) {
        String title = "접수한 민원에 답변이 등록되었어요!";

        Notification notification = Notification.builder()
                .boardId(complaint.getId())
                .title(title)
                .body(complaint.getTitle())
                .notificationType(NotificationType.COMPLAINT)
                .apiType(ApiType.COMPLAINT)
                .build();

        notificationRepository.save(notification);

        User user = complaint.getUser();

        UserNotification userNotification = UserNotification.of(user, notification);
        userNotificationRepository.save(userNotification);

        fcmMessageService.sendNotification(user, title, notification.getBody());
    }

    public void sendAndSaveComplaintStatusNotification(Complaint complaint) {
        String title = null;

        if (complaint.getStatus() == ComplaintStatus.ASSIGNED) {
            title = "접수한 민원에 담당자가 배정되었어요!";
        }
        if (complaint.getStatus() == ComplaintStatus.IN_PROGRESS) {
            title = "접수한 민원이 처리중입니다!";
        }
        if (complaint.getStatus() == ComplaintStatus.COMPLETED) {
            title = "접수한 민원의 처리가 완료되었어요!";
        }
        if (complaint.getStatus() == ComplaintStatus.REJECTION) {
            title = "접수한 민원이 반려되었어요!";
        }

        Notification notification = Notification.builder()
                .boardId(complaint.getId())
                .title(title)
                .body(complaint.getTitle())
                .notificationType(NotificationType.COMPLAINT)
                .apiType(ApiType.COMPLAINT)
                .build();

        notificationRepository.save(notification);

        User user = complaint.getUser();

        UserNotification userNotification = UserNotification.of(user, notification);
        userNotificationRepository.save(userNotification);

        fcmMessageService.sendNotification(user, title, notification.getBody());
    }

    public void sendAndSaveExpeditedComplaintNotification(Complaint complaint) {
        String title = null;

        if (complaint.getType() == ComplaintType.SMOKING) {
            title = "새로운 흡연 민원이 작성되었습니다!";
        }
        if  (complaint.getType() == ComplaintType.NOISE) {
            title = "새로운 소음 민원이 작성되었습니다!";
        }

        Notification notification = Notification.builder()
                .boardId(complaint.getId())
                .title(title)
                .body(complaint.getTitle())
                .notificationType(NotificationType.COMPLAINT)
                .apiType(ApiType.COMPLAINT)
                .build();

        notificationRepository.save(notification);

        List<User> expeditedComplaintManagers = userRepository.findByRole(Role.ROLE_DORM_EXPEDITED_COMPLAINT_MANAGER);
        for (User expeditedComplaintManager : expeditedComplaintManagers) {
            UserNotification userNotification = UserNotification.of(expeditedComplaintManager, notification);
            userNotificationRepository.save(userNotification);
            fcmMessageService.sendNotification(expeditedComplaintManager, title, notification.getBody());
        }
    }
}
