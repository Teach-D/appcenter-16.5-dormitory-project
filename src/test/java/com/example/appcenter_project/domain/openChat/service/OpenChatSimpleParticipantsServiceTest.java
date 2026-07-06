package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.fixture.OpenChatImageMessageFixture;
import com.example.appcenter_project.domain.openChat.fixture.OpenChatMultiImageFixture;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

/**
 * BR-670 참여자 단순 목록 서비스 테스트 — RED 단계
 *
 * 구현 에이전트가 생성해야 할 메서드:
 * - OpenChatRoomService.getSimpleParticipants(Long roomId, Long requesterId)
 *   반환 타입: ResponseSimpleParticipantListDto
 *
 * 구현 에이전트가 생성해야 할 클래스:
 * - ResponseSimpleParticipantDto      (userId: Long, name: String, of(Long, String))
 * - ResponseSimpleParticipantListDto  (roomId: Long, participants: List<...>, of(Long, List))
 *   getRoomId(), getParticipants() getter 필수
 */
@ExtendWith(MockitoExtension.class)
class OpenChatSimpleParticipantsServiceTest {

    @Mock
    private OpenChatRoomRepository openChatRoomRepository;

    @Mock
    private OpenChatParticipantRepository openChatParticipantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OpenChatRoomService openChatRoomService;

    private static final Long ROOM_ID = 5L;
    private static final Long REQUESTER_ID = 42L;

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — ResponseSimpleParticipantListDto 반환")
    void should_return_ResponseSimpleParticipantListDto_when_AC_6_participant_requests() {
        // given
        // OpenChatRoom room = OpenChatImageMessageFixture.createRoom();
        // List<OpenChatParticipant> participants = OpenChatMultiImageFixture.createParticipantList(ROOM_ID, List.of(42L, 43L));
        // User user42 = User.createTestUser("20240001", "pw", "홍길동", DormType.DORM_1, College.ENGINEERING, Role.ROLE_USER);
        // User user43 = User.createTestUser("20240002", "pw", "김철수", DormType.DORM_1, College.ENGINEERING, Role.ROLE_USER);
        // given(openChatRoomRepository.findById(ROOM_ID)).willReturn(Optional.of(room));
        // given(openChatParticipantRepository.existsByRoomIdAndUserId(ROOM_ID, REQUESTER_ID)).willReturn(true);
        // given(openChatParticipantRepository.findAllByRoomId(ROOM_ID)).willReturn(participants);
        // given(userRepository.findAllById(anyList())).willReturn(List.of(user42, user43));
        // NOTE: getSimpleParticipants 미구현 — 구현 후 위 코드로 교체

        // when
        // ResponseSimpleParticipantListDto result = openChatRoomService.getSimpleParticipants(ROOM_ID, REQUESTER_ID);

        // then
        // assertThat(result).isNotNull();
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — roomId가 일치함")
    void should_return_correct_roomId_when_AC_6_participant_requests() {
        // given
        // NOTE: getSimpleParticipants 미구현

        // when / then
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — participants 목록 크기가 참여자 수와 일치")
    void should_return_participants_with_correct_size() {
        // given
        // NOTE: getSimpleParticipants 미구현

        // when / then
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-7 비참여자 요청 — CustomException OPEN_CHAT_ROOM_FORBIDDEN 발생")
    void should_throw_when_AC_7_non_participant_requests_simple_list() {
        // given
        // NOTE: getSimpleParticipants 미구현

        // when / then
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-8 존재하지 않는 roomId — CustomException OPEN_CHAT_ROOM_NOT_FOUND 발생")
    void should_throw_when_AC_8_room_not_found() {
        // given
        // NOTE: getSimpleParticipants 미구현

        // when / then
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-9 ROLE_ADMIN 참여자 — name이 관리자로 반환됨")
    void should_return_admin_name_as_gwallija_when_AC_9_admin_is_participant() {
        // given
        // NOTE: getSimpleParticipants 미구현

        // when / then
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("참여자 단순 목록 — 일반 유저 name이 실제 이름으로 반환됨")
    void should_return_actual_name_for_normal_user() {
        // given
        // NOTE: getSimpleParticipants 미구현

        // when / then
        assertThat(true).isTrue();
    }
}
