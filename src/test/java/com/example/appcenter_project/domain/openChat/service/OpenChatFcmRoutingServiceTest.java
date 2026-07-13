package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import com.example.appcenter_project.domain.fcm.repository.FcmOutboxRepository;
import com.example.appcenter_project.domain.openChat.dto.UnreadNotificationInfo;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OpenChatFcmRoutingServiceTest {

    @Mock
    OpenChatParticipantRepository participantRepository;

    @Mock
    OpenChatRoomRepository openChatRoomRepository;

    @Mock
    FcmTokenRepository fcmTokenRepository;

    @Mock
    FcmOutboxRepository fcmOutboxRepository;

    @InjectMocks
    OpenChatNotificationService openChatNotificationService;

    @Test
    @DisplayName("FcmOutbox routingType = CHAT 저장 — AC-2: 채팅 즉시 알림 routing 저장")
    void should_save_outbox_with_routingType_CHAT_when_immediate_notification_sent() {
        // given
        Long roomId = 1234L;
        Long userId = 1L;
        String title = "채팅방 이름";
        String body = "새 메시지가 도착했습니다.";

        OpenChatParticipant participant = OpenChatParticipant.create(roomId, userId, false);
        FcmToken fcmToken = buildFcmToken(userId, "token-user-1");

        given(participantRepository.findAllByRoomIdAndNotificationMode(eq(roomId), any()))
                .willReturn(List.of(participant));
        given(fcmTokenRepository.findAllByUserIdIn(anyList()))
                .willReturn(List.of(fcmToken));

        ArgumentCaptor<List<FcmOutbox>> captor = ArgumentCaptor.forClass(List.class);

        // when
        openChatNotificationService.sendImmediateNotifications(roomId, Set.of(), title, body);

        // then
        then(fcmOutboxRepository).should().saveAll(captor.capture());
        List<FcmOutbox> saved = captor.getValue();
        assertThat(saved).allMatch(o -> o.getRoutingType() == FcmRoutingType.CHAT);
    }

    @Test
    @DisplayName("FcmOutbox routingId = 1234 저장 — AC-2: 채팅 즉시 알림 roomId 라우팅 저장")
    void should_save_outbox_with_routingId_roomId_when_immediate_notification_sent() {
        // given
        Long roomId = 1234L;
        Long userId = 1L;
        String title = "채팅방 이름";
        String body = "새 메시지가 도착했습니다.";

        OpenChatParticipant participant = OpenChatParticipant.create(roomId, userId, false);
        FcmToken fcmToken = buildFcmToken(userId, "token-user-1");

        given(participantRepository.findAllByRoomIdAndNotificationMode(eq(roomId), any()))
                .willReturn(List.of(participant));
        given(fcmTokenRepository.findAllByUserIdIn(anyList()))
                .willReturn(List.of(fcmToken));

        ArgumentCaptor<List<FcmOutbox>> captor = ArgumentCaptor.forClass(List.class);

        // when
        openChatNotificationService.sendImmediateNotifications(roomId, Set.of(), title, body);

        // then
        then(fcmOutboxRepository).should().saveAll(captor.capture());
        List<FcmOutbox> saved = captor.getValue();
        assertThat(saved).allMatch(o -> o.getRoutingId().equals(1234L));
    }

    @Test
    @DisplayName("FcmOutbox routingType = CHAT 저장 — AC-3: 채팅 묶음 알림 routing 저장")
    void should_save_outbox_with_routingType_CHAT_when_hourly_bundled_notification_sent() {
        // given
        Long roomId = 999L;
        Long userId = 1L;
        UnreadNotificationInfo info = new UnreadNotificationInfo(userId, roomId, 3);

        OpenChatRoom room = OpenChatRoom.createForTest(roomId, "테스트 채팅방");
        FcmToken fcmToken = buildFcmToken(userId, "token-user-1");

        given(participantRepository.findUnreadCountsForNotification()).willReturn(List.of(info));
        given(openChatRoomRepository.findAllById(any())).willReturn(List.of(room));
        given(fcmTokenRepository.findAllByUserIdIn(anyList())).willReturn(List.of(fcmToken));

        ArgumentCaptor<List<FcmOutbox>> captor = ArgumentCaptor.forClass(List.class);

        // when
        openChatNotificationService.sendHourlyUnreadNotifications();

        // then
        then(fcmOutboxRepository).should().saveAll(captor.capture());
        List<FcmOutbox> saved = captor.getValue();
        assertThat(saved).allMatch(o -> o.getRoutingType() == FcmRoutingType.CHAT);
    }

    @Test
    @DisplayName("FcmOutbox routingId = 999 저장 — AC-3: 채팅 묶음 알림 roomId 라우팅 저장")
    void should_save_outbox_with_routingId_999_when_hourly_bundled_notification_sent() {
        // given
        Long roomId = 999L;
        Long userId = 1L;
        UnreadNotificationInfo info = new UnreadNotificationInfo(userId, roomId, 3);

        OpenChatRoom room = OpenChatRoom.createForTest(roomId, "테스트 채팅방");
        FcmToken fcmToken = buildFcmToken(userId, "token-user-1");

        given(participantRepository.findUnreadCountsForNotification()).willReturn(List.of(info));
        given(openChatRoomRepository.findAllById(any())).willReturn(List.of(room));
        given(fcmTokenRepository.findAllByUserIdIn(anyList())).willReturn(List.of(fcmToken));

        ArgumentCaptor<List<FcmOutbox>> captor = ArgumentCaptor.forClass(List.class);

        // when
        openChatNotificationService.sendHourlyUnreadNotifications();

        // then
        then(fcmOutboxRepository).should().saveAll(captor.capture());
        List<FcmOutbox> saved = captor.getValue();
        assertThat(saved).allMatch(o -> o.getRoutingId().equals(999L));
    }

    private FcmToken buildFcmToken(Long userId, String token) {
        User user = User.createForTest(userId, "user-" + userId);
        return FcmToken.builder().user(user).token(token).build();
    }
}
