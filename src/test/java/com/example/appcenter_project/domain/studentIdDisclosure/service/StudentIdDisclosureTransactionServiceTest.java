package com.example.appcenter_project.domain.studentIdDisclosure.service;

import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.roommate.repository.RoommateChattingRoomRepository;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.entity.StudentIdDisclosureRequest;
import com.example.appcenter_project.domain.studentIdDisclosure.repository.StudentIdDisclosureRequestRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentIdDisclosureTransactionService — 방 타입 판별(멤버십 기반)")
class StudentIdDisclosureTransactionServiceTest {

    @Mock private StudentIdDisclosureRequestRepository disclosureRequestRepository;
    @Mock private OpenChatRoomRepository openChatRoomRepository;
    @Mock private OpenChatParticipantRepository openChatParticipantRepository;
    @Mock private RoommateChattingRoomRepository roommateChattingRoomRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private StudentIdDisclosureTransactionService transactionService;

    private static final Long REQUESTER_ID = 1L;
    private static final Long TARGET_ID = 2L;
    private static final Long COLLISION_ROOM_ID = 1001L; // open_chat_room·roommate_chatting_room 양쪽에 존재

    private User user(Long id, String studentNumber) {
        User u = User.createTestUser(studentNumber, "password", "name", null, null, Role.ROLE_USER);
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private RoommateChattingRoom roommateRoom(User host, User guest) {
        return RoommateChattingRoom.builder().host(host).guest(guest).build();
    }

    private RequestCreateDisclosureDto dto(Long roomId, Long targetId) {
        return RequestCreateDisclosureDto.builder().roomId(roomId).targetId(targetId).build();
    }

    // ── 핵심 재현 테스트 (수정 전 23005로 실패) ──────────────────────────────

    @Test
    @DisplayName("saveRequest — roomId가 오픈채팅·룸메이트 양쪽에 존재해도, 룸메이트 참여자면 성공하고 isRoommateRoom=true")
    void should_resolve_roommate_when_roomId_collides_with_open_chat() {
        RequestCreateDisclosureDto dto = dto(COLLISION_ROOM_ID, TARGET_ID);
        RoommateChattingRoom room = roommateRoom(user(REQUESTER_ID, "20230001"), user(TARGET_ID, "20230002"));

        given(openChatRoomRepository.existsById(COLLISION_ROOM_ID)).willReturn(true);
        given(openChatParticipantRepository.existsByRoomIdAndUserId(COLLISION_ROOM_ID, REQUESTER_ID)).willReturn(false);
        given(roommateChattingRoomRepository.findById(COLLISION_ROOM_ID)).willReturn(Optional.of(room));

        StudentIdDisclosureRequest saved = StudentIdDisclosureRequest.create(REQUESTER_ID, TARGET_ID, COLLISION_ROOM_ID);
        ReflectionTestUtils.setField(saved, "id", 10L);
        given(disclosureRequestRepository.save(any())).willReturn(saved);

        StudentIdDisclosureTransactionService.SaveResult result = transactionService.saveRequest(REQUESTER_ID, dto);

        assertThat(result.isRoommateRoom()).isTrue();
        assertThat(result.savedId()).isEqualTo(10L);
    }

    // ── 회귀 테스트 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("saveRequest — 순수 오픈채팅방(양쪽 참여자)이면 isRoommateRoom=false")
    void should_resolve_open_chat_when_both_are_participants() {
        RequestCreateDisclosureDto dto = dto(COLLISION_ROOM_ID, TARGET_ID);

        given(openChatRoomRepository.existsById(COLLISION_ROOM_ID)).willReturn(true);
        given(openChatParticipantRepository.existsByRoomIdAndUserId(COLLISION_ROOM_ID, REQUESTER_ID)).willReturn(true);
        given(openChatParticipantRepository.existsByRoomIdAndUserId(COLLISION_ROOM_ID, TARGET_ID)).willReturn(true);

        StudentIdDisclosureRequest saved = StudentIdDisclosureRequest.create(REQUESTER_ID, TARGET_ID, COLLISION_ROOM_ID);
        ReflectionTestUtils.setField(saved, "id", 11L);
        given(disclosureRequestRepository.save(any())).willReturn(saved);

        StudentIdDisclosureTransactionService.SaveResult result = transactionService.saveRequest(REQUESTER_ID, dto);

        assertThat(result.isRoommateRoom()).isFalse();
    }

    @Test
    @DisplayName("saveRequest — 겹치지 않는 룸메이트방이면 isRoommateRoom=true")
    void should_resolve_roommate_when_open_chat_absent() {
        RequestCreateDisclosureDto dto = dto(COLLISION_ROOM_ID, TARGET_ID);
        RoommateChattingRoom room = roommateRoom(user(REQUESTER_ID, "20230001"), user(TARGET_ID, "20230002"));

        given(openChatRoomRepository.existsById(COLLISION_ROOM_ID)).willReturn(false);
        given(roommateChattingRoomRepository.findById(COLLISION_ROOM_ID)).willReturn(Optional.of(room));

        StudentIdDisclosureRequest saved = StudentIdDisclosureRequest.create(REQUESTER_ID, TARGET_ID, COLLISION_ROOM_ID);
        ReflectionTestUtils.setField(saved, "id", 12L);
        given(disclosureRequestRepository.save(any())).willReturn(saved);

        StudentIdDisclosureTransactionService.SaveResult result = transactionService.saveRequest(REQUESTER_ID, dto);

        assertThat(result.isRoommateRoom()).isTrue();
    }

    @Test
    @DisplayName("saveRequest — 어느 테이블에도 방이 없으면 OPEN_CHAT_ROOM_NOT_FOUND")
    void should_throw_not_found_when_room_absent_in_both() {
        RequestCreateDisclosureDto dto = dto(COLLISION_ROOM_ID, TARGET_ID);

        given(openChatRoomRepository.existsById(COLLISION_ROOM_ID)).willReturn(false);
        given(roommateChattingRoomRepository.findById(COLLISION_ROOM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.saveRequest(REQUESTER_ID, dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("saveRequest — 방은 존재하나 두 사용자가 함께 속한 방이 아니면 DISCLOSURE_NOT_IN_SAME_ROOM")
    void should_throw_not_in_same_room_when_users_not_members() {
        RequestCreateDisclosureDto dto = dto(COLLISION_ROOM_ID, TARGET_ID);
        RoommateChattingRoom room = roommateRoom(user(998L, "20239998"), user(999L, "20239999"));

        given(openChatRoomRepository.existsById(COLLISION_ROOM_ID)).willReturn(true);
        given(openChatParticipantRepository.existsByRoomIdAndUserId(COLLISION_ROOM_ID, REQUESTER_ID)).willReturn(false);
        given(roommateChattingRoomRepository.findById(COLLISION_ROOM_ID)).willReturn(Optional.of(room));

        assertThatThrownBy(() -> transactionService.saveRequest(REQUESTER_ID, dto))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DISCLOSURE_NOT_IN_SAME_ROOM);
    }

    // ── accept 라우팅 회귀 (겹치는 id의 룸메이트 요청) ──────────────────────

    @Test
    @DisplayName("acceptRequest — 겹치는 id의 룸메이트 요청 수락 시 isRoommateRoom=true 로 정확히 라우팅")
    void should_route_roommate_on_accept_when_roomId_collides() {
        Long requestId = 20L;
        StudentIdDisclosureRequest request = StudentIdDisclosureRequest.create(REQUESTER_ID, TARGET_ID, COLLISION_ROOM_ID);
        ReflectionTestUtils.setField(request, "id", requestId);
        User requester = user(REQUESTER_ID, "20230001");
        RoommateChattingRoom room = roommateRoom(requester, user(TARGET_ID, "20230002"));

        given(disclosureRequestRepository.findById(requestId)).willReturn(Optional.of(request));
        given(userRepository.findById(REQUESTER_ID)).willReturn(Optional.of(requester));
        given(openChatRoomRepository.existsById(COLLISION_ROOM_ID)).willReturn(true);
        given(openChatParticipantRepository.existsByRoomIdAndUserId(COLLISION_ROOM_ID, REQUESTER_ID)).willReturn(false);
        given(roommateChattingRoomRepository.findById(COLLISION_ROOM_ID)).willReturn(Optional.of(room));

        StudentIdDisclosureTransactionService.AcceptResult result = transactionService.acceptRequest(TARGET_ID, requestId);

        assertThat(result.isRoommateRoom()).isTrue();
        assertThat(result.dto().getRequesterStudentNumber()).isEqualTo("20230001");
    }
}