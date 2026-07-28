package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportNotificationService {

    private static final String TITLE = "새 신고 접수";
    private static final String BODY = "새로운 신고가 접수되었습니다. 신고 목록에서 확인해주세요.";
    private static final String APPROVED_TITLE = "신고 처리 완료";
    private static final String APPROVED_BODY = "신고하신 내용이 검토되어 조치되었습니다.";

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final FcmMessageService fcmMessageService;

    public void notifyAdminsNewReport(Long reportId) {
        List<User> admins = userRepository.findByRole(Role.ROLE_ADMIN);
        if (admins.isEmpty()) {
            log.warn("[Report] 알림 대상 관리자가 없습니다. reportId={}", reportId);
            return;
        }

        Notification notification = notificationRepository.save(
                Notification.of(TITLE, BODY, NotificationType.REPORT, null, reportId));

        for (User admin : admins) {
            userNotificationRepository.save(UserNotification.of(admin, notification));
            try {
                fcmMessageService.sendNotification(admin, TITLE, BODY);
            } catch (Exception e) {
                log.error("[Report] 관리자 신고 알림 발송 실패 - 관리자: {}, reportId: {}", admin.getId(), reportId, e);
            }
        }
    }

    // 신고 승인 시 신고자 본인에게만 알림 (반려/취소·대상 유저에겐 발송하지 않음)
    public void notifyReporterApproved(Long reporterId) {
        User reporter = userRepository.findById(reporterId).orElse(null);
        if (reporter == null) {
            log.warn("[Report] 신고자를 찾을 수 없어 승인 알림 생략. reporterId={}", reporterId);
            return;
        }
        Notification notification = notificationRepository.save(
                Notification.of(APPROVED_TITLE, APPROVED_BODY, NotificationType.REPORT, null, null));
        userNotificationRepository.save(UserNotification.of(reporter, notification));
        try {
            fcmMessageService.sendNotification(reporter, APPROVED_TITLE, APPROVED_BODY);
        } catch (Exception e) {
            log.error("[Report] 신고자 승인 알림 발송 실패 - reporterId: {}", reporterId, e);
        }
    }
}