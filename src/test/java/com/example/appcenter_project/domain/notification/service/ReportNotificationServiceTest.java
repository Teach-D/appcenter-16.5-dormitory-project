package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportNotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock UserNotificationRepository userNotificationRepository;
    @Mock UserRepository userRepository;
    @Mock FcmMessageService fcmMessageService;

    @InjectMocks ReportNotificationService reportNotificationService;

    @Test
    @DisplayName("관리자 전원에게 인앱 알림 저장 + FCM 푸시를 발송한다")
    void notifyAdmins() {
        User admin1 = User.createForTest(1L, "관리자1");
        User admin2 = User.createForTest(2L, "관리자2");
        given(userRepository.findByRole(Role.ROLE_ADMIN)).willReturn(List.of(admin1, admin2));
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));

        reportNotificationService.notifyAdminsNewReport(100L);

        verify(notificationRepository).save(any(Notification.class));
        verify(userNotificationRepository, times(2)).save(any(UserNotification.class));
        verify(fcmMessageService).sendNotification(admin1, "새 신고 접수", "새로운 신고가 접수되었습니다. 신고 목록에서 확인해주세요.");
        verify(fcmMessageService).sendNotification(admin2, "새 신고 접수", "새로운 신고가 접수되었습니다. 신고 목록에서 확인해주세요.");
    }

    @Test
    @DisplayName("관리자가 없으면 아무것도 발송하지 않는다")
    void noAdmins() {
        given(userRepository.findByRole(Role.ROLE_ADMIN)).willReturn(List.of());

        reportNotificationService.notifyAdminsNewReport(100L);

        verify(notificationRepository, never()).save(any());
        verify(fcmMessageService, never()).sendNotification(any(), any(), any());
    }

    @Test
    @DisplayName("승인 시 신고자 본인에게 알림을 발송한다")
    void notifyReporter() {
        User reporter = User.createForTest(417L, "신고자");
        given(userRepository.findById(417L)).willReturn(Optional.of(reporter));
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> inv.getArgument(0));

        reportNotificationService.notifyReporterApproved(417L);

        verify(userNotificationRepository).save(any(UserNotification.class));
        verify(fcmMessageService).sendNotification(reporter, "신고 처리 완료", "신고하신 내용이 검토되어 조치되었습니다.");
    }
}