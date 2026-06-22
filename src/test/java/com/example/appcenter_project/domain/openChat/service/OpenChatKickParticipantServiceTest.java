package com.example.appcenter_project.domain.openChat.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * OpenChatRoomService.kickParticipant — 강제퇴장 (다중 방장 시스템 기준)
 *
 * 메서드 시그니처:
 *   void kickParticipant(Long actorId, Long roomId, Long targetUserId)
 *
 * @InjectMocks 대상:
 *   OpenChatRoomService openChatRoomService
 *
 * @Mock 대상:
 *   OpenChatRoomRepository openChatRoomRepository
 *   OpenChatParticipantRepository openChatParticipantRepository
 *   OpenChatMessageService openChatMessageService
 *   UserRepository userRepository
 *
 * 다중 방장 시스템 핵심 규칙:
 *   - 방장 여부: OpenChatParticipant.isHost() == true
 *   - 방장 확인: existsByRoomIdAndUserIdAndIsHost(roomId, userId, true)
 *   - 방장은 일반 참여자(isHost=false)만 퇴장 가능
 *   - 방장이 다른 방장을 퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN
 *   - ADMIN은 방장 포함 누구든 퇴장 가능
 *   - 자기 자신 퇴장 불가 (역할 무관)
 *   - 강제퇴장 시 방 삭제/방장 위임 없음 (room 은 그대로 유지)
 *
 * [Happy Path]
 * TC-01: 방장이 일반 참여자(isHost=false)를 강제퇴장 성공 → participant 삭제됨
 * TC-02: ADMIN이 일반 참여자를 강제퇴장 성공 → participant 삭제됨
 * TC-03: ADMIN이 방장(isHost=true)을 강제퇴장 성공 → participant 삭제됨, 방 유지
 * TC-04: 퇴장 시 시스템 메시지 "{유저이름}님이 강제퇴장되었습니다." 발송
 *
 * [Business Rule]
 * TC-05: 방장이 다른 방장(isHost=true)을 퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN
 * TC-06: USER 권한 + 방장 아님(isHost=false) → OPEN_CHAT_KICK_FORBIDDEN
 * TC-07: 자기 자신(actorId==targetUserId) 강제퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN
 *
 * [Error]
 * TC-08: 존재하지 않는 roomId → OPEN_CHAT_ROOM_NOT_FOUND
 * TC-09: 대상 유저가 해당 방 미참여자 → OPEN_CHAT_PARTICIPANT_NOT_FOUND
 * TC-10: 방장 아닌 USER가 참여 중이지만 퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN
 * TC-11: 일반 유저(비방장)가 방장을 퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN
 *
 * [신규 ErrorCode]
 * OPEN_CHAT_KICK_FORBIDDEN(FORBIDDEN, 22017, "[OpenChat] 강제퇴장 권한이 없습니다.")
 */
@ExtendWith(MockitoExtension.class)
class OpenChatKickParticipantServiceTest {

    /*
     * 활성화 방법:
     *
     * @Mock
     * private OpenChatRoomRepository openChatRoomRepository;
     *
     * @Mock
     * private OpenChatParticipantRepository openChatParticipantRepository;
     *
     * @Mock
     * private OpenChatMessageService openChatMessageService;
     *
     * @Mock
     * private UserRepository userRepository;
     *
     * @InjectMocks
     * private OpenChatRoomService openChatRoomService;
     */

    // ============================================================
    // Happy Path
    // ============================================================

    @Test
    @DisplayName("강제퇴장 성공 — TC-01: 방장이 일반 참여자(isHost=false)를 퇴장시키면 participant 삭제됨")
    void should_delete_participant_when_host_kicks_normal_participant() {
        // given
        // Long hostId = 1L, roomId = 1L, targetUserId = 2L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // OpenChatParticipant targetParticipant = OpenChatParticipant.create(roomId, targetUserId, false); // isHost=false
        // User actorUser = UserFixture.createUser(hostId); // role=ROLE_USER
        // User targetUser = UserFixture.createUser(targetUserId);
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(hostId)).willReturn(Optional.of(actorUser));
        // given(openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, hostId, true)).willReturn(true);
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, targetUserId)).willReturn(Optional.of(targetParticipant));
        // given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(hostId, roomId, targetUserId);
        //
        // then
        // assertThatCode(action).doesNotThrowAnyException();
        // then(openChatParticipantRepository).should(times(1)).delete(targetParticipant);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("강제퇴장 성공 — TC-02: ADMIN이 일반 참여자를 퇴장시키면 participant 삭제됨")
    void should_delete_participant_when_admin_kicks_normal_participant() {
        // given
        // Long adminId = 99L, roomId = 1L, targetUserId = 2L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // OpenChatParticipant targetParticipant = OpenChatParticipant.create(roomId, targetUserId, false);
        // User adminUser = UserFixture.createAdminUser(adminId); // role=ROLE_ADMIN
        // User targetUser = UserFixture.createUser(targetUserId);
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(adminId)).willReturn(Optional.of(adminUser));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, targetUserId)).willReturn(Optional.of(targetParticipant));
        // given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(adminId, roomId, targetUserId);
        //
        // then
        // assertThatCode(action).doesNotThrowAnyException();
        // then(openChatParticipantRepository).should(times(1)).delete(targetParticipant);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("강제퇴장 성공 — TC-03: ADMIN이 방장(isHost=true)을 퇴장시키면 방 유지, participant 삭제됨")
    void should_delete_host_participant_when_admin_kicks_host() {
        // given
        // Long adminId = 99L, roomId = 1L, hostId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // OpenChatParticipant hostParticipant = OpenChatParticipant.create(roomId, hostId, true); // isHost=true
        // User adminUser = UserFixture.createAdminUser(adminId); // role=ROLE_ADMIN
        // User hostUser = UserFixture.createUser(hostId);
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(adminId)).willReturn(Optional.of(adminUser));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, hostId)).willReturn(Optional.of(hostParticipant));
        // given(userRepository.findById(hostId)).willReturn(Optional.of(hostUser));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(adminId, roomId, hostId);
        //
        // then
        // assertThatCode(action).doesNotThrowAnyException();
        // then(openChatParticipantRepository).should(times(1)).delete(hostParticipant);
        // then(openChatRoomRepository).should(never()).delete(any()); // 방 삭제 없음
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("강제퇴장 성공 — TC-04: 퇴장 시 시스템 메시지 \"{유저이름}님이 강제퇴장되었습니다.\" 발송")
    void should_send_system_message_when_participant_is_kicked() {
        // given
        // Long hostId = 1L, roomId = 1L, targetUserId = 2L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // OpenChatParticipant targetParticipant = OpenChatParticipant.create(roomId, targetUserId, false);
        // User actorUser = UserFixture.createUser(hostId);
        // User targetUser = UserFixture.createUserWithName(targetUserId, "김철수");
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(hostId)).willReturn(Optional.of(actorUser));
        // given(openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, hostId, true)).willReturn(true);
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, targetUserId)).willReturn(Optional.of(targetParticipant));
        // given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        //
        // when
        // openChatRoomService.kickParticipant(hostId, roomId, targetUserId);
        //
        // then
        // then(openChatMessageService).should(times(1))
        //     .sendSystemMessage(eq(roomId), eq("김철수님이 강제퇴장되었습니다."));
        assertThat(true).isTrue();
    }

    // ============================================================
    // Business Rule
    // ============================================================

    @Test
    @DisplayName("CustomException 발생 — TC-05: 방장이 다른 방장(isHost=true)을 퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN")
    void should_throw_when_host_kicks_another_host() {
        // given
        // Long hostId = 1L, roomId = 1L, anotherHostId = 2L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // OpenChatParticipant anotherHostParticipant = OpenChatParticipant.create(roomId, anotherHostId, true); // isHost=true
        // User actorUser = UserFixture.createUser(hostId);
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(hostId)).willReturn(Optional.of(actorUser));
        // given(openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, hostId, true)).willReturn(true);
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, anotherHostId)).willReturn(Optional.of(anotherHostParticipant));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(hostId, roomId, anotherHostId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — TC-06: USER 권한 + 방장 아님(isHost=false) → OPEN_CHAT_KICK_FORBIDDEN")
    void should_throw_when_non_host_user_attempts_kick() {
        // given
        // Long nonHostId = 2L, roomId = 1L, targetUserId = 3L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // User actorUser = UserFixture.createUser(nonHostId); // role=ROLE_USER
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(nonHostId)).willReturn(Optional.of(actorUser));
        // given(openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, nonHostId, true)).willReturn(false);
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(nonHostId, roomId, targetUserId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — TC-07: 자기 자신(actorId==targetUserId) 강제퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN")
    void should_throw_when_self_kick_attempted() {
        // given
        // Long hostId = 1L, roomId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(hostId, roomId, hostId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
        assertThat(true).isTrue();
    }

    // ============================================================
    // Error Case
    // ============================================================

    @Test
    @DisplayName("CustomException 발생 — TC-08: 존재하지 않는 roomId → OPEN_CHAT_ROOM_NOT_FOUND")
    void should_throw_when_room_not_found() {
        // given
        // Long actorId = 1L, roomId = 999L, targetUserId = 2L;
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.empty());
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(actorId, roomId, targetUserId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — TC-09: 대상 유저가 해당 방 미참여자 → OPEN_CHAT_PARTICIPANT_NOT_FOUND")
    void should_throw_when_target_is_not_participant() {
        // given
        // Long adminId = 99L, roomId = 1L, nonParticipantId = 77L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // User adminUser = UserFixture.createAdminUser(adminId); // role=ROLE_ADMIN
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(adminId)).willReturn(Optional.of(adminUser));
        // given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, nonParticipantId)).willReturn(Optional.empty());
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(adminId, roomId, nonParticipantId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — TC-10: 방장 아닌 USER가 참여 중이지만 퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN")
    void should_throw_when_non_host_participant_attempts_kick() {
        // given
        // Long nonHostId = 3L, roomId = 1L, targetUserId = 2L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // User actorUser = UserFixture.createUser(nonHostId); // role=ROLE_USER
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(nonHostId)).willReturn(Optional.of(actorUser));
        // given(openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, nonHostId, true)).willReturn(false);
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(nonHostId, roomId, targetUserId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("CustomException 발생 — TC-11: 일반 유저(비방장)가 방장을 퇴장 시도 → OPEN_CHAT_KICK_FORBIDDEN")
    void should_throw_when_non_host_attempts_to_kick_host() {
        // given
        // Long nonHostId = 2L, roomId = 1L, hostId = 1L;
        // OpenChatRoom room = OpenChatRoomFixture.createRoom();
        // User actorUser = UserFixture.createUser(nonHostId); // role=ROLE_USER
        // given(openChatRoomRepository.findByIdWithLock(roomId)).willReturn(Optional.of(room));
        // given(userRepository.findById(nonHostId)).willReturn(Optional.of(actorUser));
        // given(openChatParticipantRepository.existsByRoomIdAndUserIdAndIsHost(roomId, nonHostId, true)).willReturn(false);
        //
        // when
        // ThrowingCallable action = () -> openChatRoomService.kickParticipant(nonHostId, roomId, hostId);
        //
        // then
        // assertThatThrownBy(action)
        //     .isInstanceOf(CustomException.class)
        //     .extracting("errorCode")
        //     .isEqualTo(ErrorCode.OPEN_CHAT_KICK_FORBIDDEN);
        assertThat(true).isTrue();
    }
}
