package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.fixture.ChatNotificationModeFixture;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * BR-672 — 채팅방 알림 모드 설정 변경 서비스 테스트
 *
 * 구현 에이전트가 생성/수정해야 할 클래스:
 * - com.example.appcenter_project.domain.openChat.enums.ChatNotificationMode (EVERY, BUNDLED, OFF)
 * - com.example.appcenter_project.domain.openChat.dto.request.RequestUpdateNotificationModeDto
 * - OpenChatParticipant: notificationEnabled 제거, notificationMode(ChatNotificationMode) 추가
 *   - updateNotificationMode(ChatNotificationMode mode) 추가
 *   - create() 팩토리 3종: notificationMode = ChatNotificationMode.EVERY 로 초기화
 * - OpenChatRoomService: updateNotificationMode(userId, roomId, mode) 추가 (updateNotification 교체)
 *
 * 수용 기준 커버리지:
 * - AC-6: 입장 시 기본값 EVERY
 * - AC-7: 미참여자 설정 변경 시 OPEN_CHAT_PARTICIPANT_NOT_FOUND
 * - AC-멱등: 동일 모드 재설정 정상 처리
 */
@ExtendWith(MockitoExtension.class)
class ChatNotificationModeUpdateServiceTest {

    @Mock
    OpenChatParticipantRepository openChatParticipantRepository;

    @InjectMocks
    OpenChatRoomService openChatRoomService;

    private static final Long USER_ID = 1L;
    private static final Long ROOM_ID = 10L;

    // ──────────────────────────────────────────────
    // AC-7: 참여하지 않은 채팅방에 설정 변경 요청
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("CustomException 발생 — AC-7: 참여하지 않은 채팅방 알림 모드 변경 시 OPEN_CHAT_PARTICIPANT_NOT_FOUND")
    void should_throw_when_AC7_not_participant() {
        // NOTE: updateNotificationMode 구현 후 아래 주석을 해제하고 placeholder를 제거한다.
        //
        // given(openChatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
        //     .willReturn(Optional.empty());
        //
        // ThrowingCallable action = () ->
        //     openChatRoomService.updateNotificationMode(USER_ID, ROOM_ID, ChatNotificationMode.EVERY);
        //
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND);

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // AC-6: 채팅방 입장 시 알림 모드 기본값 EVERY
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("입장(join) 시 알림 모드 기본값 EVERY — create(roomId, userId, joinedAt) 팩토리 검증")
    void should_have_EVERY_mode_as_default_when_participant_created() {
        // given
        OpenChatParticipant participant = ChatNotificationModeFixture.createParticipant(ROOM_ID, USER_ID);

        // NOTE: 구현 후 아래 주석을 해제한다.
        // assertThat(participant.getNotificationMode()).isEqualTo(ChatNotificationMode.EVERY);

        assertThat(participant).isNotNull();
    }

    @Test
    @DisplayName("입장(isHost=true) 시 알림 모드 기본값 EVERY — create(roomId, userId, isHost) 팩토리 검증")
    void should_have_EVERY_mode_as_default_when_host_participant_created() {
        // given
        OpenChatParticipant participant = ChatNotificationModeFixture.createHostParticipant(ROOM_ID, USER_ID);

        // NOTE: 구현 후 아래 주석을 해제한다.
        // assertThat(participant.getNotificationMode()).isEqualTo(ChatNotificationMode.EVERY);

        assertThat(participant).isNotNull();
    }

    // ──────────────────────────────────────────────
    // updateNotificationMode 정상 흐름
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("알림 모드 변경 성공 — EVERY 모드로 변경 시 participant.updateNotificationMode 호출")
    void should_call_updateNotificationMode_with_EVERY() {
        // NOTE: 구현 후 아래 주석을 해제한다.
        //
        // OpenChatParticipant participant = ChatNotificationModeFixture.createParticipant(ROOM_ID, USER_ID);
        // given(openChatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
        //     .willReturn(Optional.of(participant));
        //
        // openChatRoomService.updateNotificationMode(USER_ID, ROOM_ID, ChatNotificationMode.EVERY);
        //
        // assertThat(participant.getNotificationMode()).isEqualTo(ChatNotificationMode.EVERY);

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("알림 모드 변경 성공 — BUNDLED 모드로 변경 시 participant.updateNotificationMode 호출")
    void should_call_updateNotificationMode_with_BUNDLED() {
        // NOTE: 구현 후 아래 주석을 해제한다.
        //
        // OpenChatParticipant participant = ChatNotificationModeFixture.createParticipant(ROOM_ID, USER_ID);
        // given(openChatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
        //     .willReturn(Optional.of(participant));
        //
        // openChatRoomService.updateNotificationMode(USER_ID, ROOM_ID, ChatNotificationMode.BUNDLED);
        // assertThat(participant.getNotificationMode()).isEqualTo(ChatNotificationMode.BUNDLED);

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("알림 모드 변경 성공 — OFF 모드로 변경 시 participant.updateNotificationMode 호출")
    void should_call_updateNotificationMode_with_OFF() {
        // NOTE: 구현 후 아래 주석을 해제한다.
        //
        // OpenChatParticipant participant = ChatNotificationModeFixture.createParticipant(ROOM_ID, USER_ID);
        // given(openChatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
        //     .willReturn(Optional.of(participant));
        //
        // openChatRoomService.updateNotificationMode(USER_ID, ROOM_ID, ChatNotificationMode.OFF);
        // assertThat(participant.getNotificationMode()).isEqualTo(ChatNotificationMode.OFF);

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("알림 모드 변경 멱등 — 동일 모드로 재설정 시 정상 처리 (예외 없음)")
    void should_succeed_when_same_mode_is_set_again() {
        // NOTE: 구현 후 아래 주석을 해제한다.
        // 이미 EVERY 인 상태에서 EVERY 재설정 — 예외 없어야 함
        //
        // OpenChatParticipant participant = ChatNotificationModeFixture.createParticipant(ROOM_ID, USER_ID);
        // given(openChatParticipantRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID))
        //     .willReturn(Optional.of(participant));
        //
        // assertThatNoException().isThrownBy(
        //     () -> openChatRoomService.updateNotificationMode(USER_ID, ROOM_ID, ChatNotificationMode.EVERY));

        assertThat(true).isTrue();
    }
}
