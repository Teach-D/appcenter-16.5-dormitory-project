package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import com.example.appcenter_project.domain.notification.dto.response.ResponseNotificationReadDto;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.openChat.service.OpenChatMessageService;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.shared.enums.ApiType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NotificationReadServiceTest {

    @Mock
    private UserNotificationRepository userNotificationQuerydslRepository;

    @Mock
    private OpenChatMessageService openChatMessageService;

    @InjectMocks
    private NotificationReadService notificationReadService;

    private static final Long USER_ID = 1L;
    private static final Long ANNOUNCEMENT_ID = 5678L;
    private static final Long CHAT_ROOM_ID = 1234L;

    // =========================================================================
    // AC-1: NOTICE 타입 — isRead 업데이트
    // =========================================================================

    @Test
    @DisplayName("AC-1: NOTICE 타입 요청 시 해당 UserNotification.isRead = true 로 변경")
    void should_mark_userNotification_as_read_when_type_is_NOTICE() {
        Notification notification = buildNotification(ANNOUNCEMENT_ID, ApiType.ANNOUNCEMENT);
        UserNotification userNotification = UserNotification.of(buildUser(USER_ID), notification);

        given(userNotificationQuerydslRepository.findByUserIdAndBoardIdAndApiType(
                USER_ID, ANNOUNCEMENT_ID, ApiType.ANNOUNCEMENT))
                .willReturn(List.of(userNotification));

        ResponseNotificationReadDto result = notificationReadService.markAsRead(
                USER_ID, FcmRoutingType.NOTICE, ANNOUNCEMENT_ID);

        assertThat(userNotification.isRead()).isTrue();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("AC-1: NOTICE 타입 — 여러 개의 UserNotification이 있어도 모두 읽음 처리")
    void should_mark_all_userNotifications_as_read_when_multiple_records_exist() {
        Notification notification = buildNotification(ANNOUNCEMENT_ID, ApiType.ANNOUNCEMENT);
        UserNotification un1 = UserNotification.of(buildUser(USER_ID), notification);
        UserNotification un2 = UserNotification.of(buildUser(USER_ID), notification);

        given(userNotificationQuerydslRepository.findByUserIdAndBoardIdAndApiType(
                USER_ID, ANNOUNCEMENT_ID, ApiType.ANNOUNCEMENT))
                .willReturn(List.of(un1, un2));

        notificationReadService.markAsRead(USER_ID, FcmRoutingType.NOTICE, ANNOUNCEMENT_ID);

        assertThat(un1.isRead()).isTrue();
        assertThat(un2.isRead()).isTrue();
    }

    // =========================================================================
    // AC-2: NOTICE 타입 — 대상 UserNotification 없어도 200 OK (멱등성)
    // =========================================================================

    @Test
    @DisplayName("AC-2: NOTICE 타입 — 대상 UserNotification 없음 시 예외 없이 success=true 반환")
    void should_return_success_when_NOTICE_userNotification_not_found() {
        given(userNotificationQuerydslRepository.findByUserIdAndBoardIdAndApiType(
                USER_ID, 999L, ApiType.ANNOUNCEMENT))
                .willReturn(List.of());

        ResponseNotificationReadDto result = notificationReadService.markAsRead(
                USER_ID, FcmRoutingType.NOTICE, 999L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("성공적으로 읽음 처리되었습니다.");
    }

    // =========================================================================
    // AC-4: CHAT 타입 — lastReadMessageId 업데이트
    // =========================================================================

    @Test
    @DisplayName("AC-4: CHAT 타입 요청 시 OpenChatMessageService.markChatRoomAsRead 위임")
    void should_delegate_to_openChatMessageService_when_type_is_CHAT() {
        willDoNothing().given(openChatMessageService).markChatRoomAsRead(CHAT_ROOM_ID, USER_ID);

        ResponseNotificationReadDto result = notificationReadService.markAsRead(
                USER_ID, FcmRoutingType.CHAT, CHAT_ROOM_ID);

        then(openChatMessageService).should(times(1)).markChatRoomAsRead(CHAT_ROOM_ID, USER_ID);
        assertThat(result.isSuccess()).isTrue();
    }

    // =========================================================================
    // AC-5: CHAT 타입 — 방에 메시지 없으면 성공 반환 (위임 후 서비스에서 처리)
    // =========================================================================

    @Test
    @DisplayName("AC-5: CHAT 타입 — 방에 메시지 없어도 success=true 반환 (OpenChatMessageService 내부 처리)")
    void should_return_success_when_CHAT_room_has_no_messages() {
        willDoNothing().given(openChatMessageService).markChatRoomAsRead(CHAT_ROOM_ID, USER_ID);

        ResponseNotificationReadDto result = notificationReadService.markAsRead(
                USER_ID, FcmRoutingType.CHAT, CHAT_ROOM_ID);

        assertThat(result.isSuccess()).isTrue();
    }

    // =========================================================================
    // AC-6: CHAT 타입 — 참여자 아님 → 예외 전파
    // =========================================================================

    @Test
    @DisplayName("AC-6: CHAT 타입 — 채팅방 참여자 아닌 경우 OPEN_CHAT_NOT_PARTICIPANT 예외 전파")
    void should_propagate_exception_when_CHAT_user_is_not_participant() {
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_NOT_PARTICIPANT))
                .given(openChatMessageService).markChatRoomAsRead(CHAT_ROOM_ID, USER_ID);

        assertThatThrownBy(() -> notificationReadService.markAsRead(
                USER_ID, FcmRoutingType.CHAT, CHAT_ROOM_ID))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_NOT_PARTICIPANT);
    }

    // =========================================================================
    // 응답 메시지 검증
    // =========================================================================

    @Test
    @DisplayName("성공 응답 — message 필드가 '성공적으로 읽음 처리되었습니다.' 고정값")
    void should_return_fixed_success_message() {
        willDoNothing().given(openChatMessageService).markChatRoomAsRead(CHAT_ROOM_ID, USER_ID);

        ResponseNotificationReadDto result = notificationReadService.markAsRead(
                USER_ID, FcmRoutingType.CHAT, CHAT_ROOM_ID);

        assertThat(result.getMessage()).isEqualTo("성공적으로 읽음 처리되었습니다.");
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Notification buildNotification(Long boardId, ApiType apiType) {
        Notification notification = Notification.builder()
                .boardId(boardId)
                .title("테스트 공지")
                .body("테스트 내용")
                .apiType(apiType)
                .build();
        ReflectionTestUtils.setField(notification, "id", boardId);
        return notification;
    }

    private User buildUser(Long userId) {
        User user = User.createForTest(userId, "user-" + userId);
        return user;
    }
}
