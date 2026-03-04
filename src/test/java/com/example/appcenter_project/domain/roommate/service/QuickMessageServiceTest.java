package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuickMessageServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    MyRoommateRepository myRoommateRepository;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserNotificationRepository userNotificationRepository;

    @Mock
    FcmMessageService fcmMessageService;

    @InjectMocks
    QuickMessageService quickMessageService;

    @Test
    @DisplayName("퀵메시지 전송 - 정상 전송")
    void sendQuickMessageToMyRoommate_정상_전송() {
        User sender = mock(User.class);
        when(sender.getId()).thenReturn(1L);
        when(sender.getName()).thenReturn("홍길동");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        User roommateUser = mock(User.class);
        when(roommateUser.getId()).thenReturn(2L);
        when(roommateUser.getReceiveNotificationTypes())
                .thenReturn(java.util.List.of(NotificationType.ROOMMATE));

        MyRoommate myRoommate = mock(MyRoommate.class);
        when(myRoommate.getRoommate()).thenReturn(roommateUser);
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        Notification savedNotification = mock(Notification.class);
        when(savedNotification.getTitle()).thenReturn("홍길동님이 퀵메시지를 보냈어요!");
        when(savedNotification.getBody()).thenReturn("안녕하세요!");
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        quickMessageService.sendQuickMessageToMyRoommate(1L, "안녕하세요!");

        verify(notificationRepository).save(any(Notification.class));
        verify(userNotificationRepository).save(any(UserNotification.class));
        verify(fcmMessageService).sendNotification(eq(roommateUser), anyString(), anyString());
    }

    @Test
    @DisplayName("퀵메시지 전송 - 보내는 유저 없으면 예외")
    void sendQuickMessageToMyRoommate_유저없으면_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quickMessageService.sendQuickMessageToMyRoommate(99L, "안녕하세요!"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    @Test
    @DisplayName("퀵메시지 전송 - MyRoommate 없으면 예외")
    void sendQuickMessageToMyRoommate_룸메이트없으면_예외() {
        User sender = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quickMessageService.sendQuickMessageToMyRoommate(1L, "안녕하세요!"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", MY_ROOMMATE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("퀵메시지 전송 - 룸메이트가 ROOMMATE 알림 비활성화 시 전송 안함")
    void sendQuickMessageToMyRoommate_알림비활성화시_전송안함() {
        User sender = mock(User.class);
        when(sender.getId()).thenReturn(1L);
        when(sender.getName()).thenReturn("홍길동");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        User roommateUser = mock(User.class);
        // ROOMMATE 알림 타입 미포함
        when(roommateUser.getReceiveNotificationTypes()).thenReturn(java.util.List.of(NotificationType.DORMITORY));

        MyRoommate myRoommate = mock(MyRoommate.class);
        when(myRoommate.getRoommate()).thenReturn(roommateUser);
        when(myRoommateRepository.findByUserId(1L)).thenReturn(Optional.of(myRoommate));

        quickMessageService.sendQuickMessageToMyRoommate(1L, "안녕하세요!");

        // FCM 전송 안됨
        verify(fcmMessageService, never()).sendNotification(any(), anyString(), anyString());
        verify(notificationRepository, never()).save(any());
    }
}