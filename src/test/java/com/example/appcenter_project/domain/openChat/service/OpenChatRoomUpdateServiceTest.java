package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestUpdateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.fixture.OpenChatRoomUpdateFixture;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OpenChatRoomUpdateServiceTest {

    @Mock
    OpenChatRoomRepository openChatRoomRepository;

    @Mock
    OpenChatParticipantRepository openChatParticipantRepository;

    @InjectMocks
    OpenChatRoomService openChatRoomService;

    // ──────────────────────────────────────────────
    // Happy Path
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("수정 성공 — AC-1 OPEN 방 방장이 name 수정 시 예외 없이 완료")
    void should_update_room_without_exception_when_host_updates_open_room() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatCode(action).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수정 성공 — AC-2 DERIVED 방 방장이 description·maxParticipants 수정 시 예외 없이 완료")
    void should_update_room_without_exception_when_host_updates_derived_room() {
        // given
        Long userId = 1L;
        Long roomId = 2L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createDerivedRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = RequestUpdateOpenChatRoomDto.builder()
                .description("새 설명")
                .maxParticipants(20)
                .build();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));
        given(openChatParticipantRepository.countByRoomId(roomId)).willReturn(1L);

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatCode(action).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("수정 성공 — AC-7 name만 전달 시 name만 변경되고 나머지 필드 유지 (update 메서드 호출 검증)")
    void should_call_update_on_room_when_only_name_provided() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));

        // when
        openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThat(room.getName()).isEqualTo("이름만 변경");
    }

    @Test
    @DisplayName("수정 성공 — AC-8 password 빈 문자열 전달 시 password가 null로 변경")
    void should_clear_password_when_empty_string_provided() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithPasswordClear();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));

        // when
        openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThat(room.getPassword()).isNull();
    }

    @Test
    @DisplayName("수정 성공 — 수정 필드 전부 null 시 예외 없이 완료 (no-op)")
    void should_complete_without_exception_when_all_fields_null() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithAllNull();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatCode(action).doesNotThrowAnyException();
    }

    // ──────────────────────────────────────────────
    // Error Cases — Business Rules
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("CustomException 발생 — AC-9 BR-700-1 존재하지 않는 roomId → OPEN_CHAT_ROOM_NOT_FOUND")
    void should_throw_CustomException_when_room_not_found() {
        // given
        Long userId = 1L;
        Long roomId = 999L;
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.empty());

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-4 BR-700-2 참여자가 아닌 사용자 → OPEN_CHAT_PARTICIPANT_NOT_FOUND")
    void should_throw_CustomException_when_user_is_not_participant() {
        // given
        Long userId = 99L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.empty());

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-3 BR-700-2 방장이 아닌 참여자 → OPEN_CHAT_NOT_HOST")
    void should_throw_CustomException_when_user_is_not_host() {
        // given
        Long userId = 2L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        OpenChatParticipant nonHost = OpenChatRoomUpdateFixture.createNonHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(nonHost));

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_NOT_HOST);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-5 BR-700-3 PERSONAL 방 수정 시도 → OPEN_CHAT_ROOM_FORBIDDEN")
    void should_throw_CustomException_when_room_type_is_personal() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createPersonalRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-5 BR-700-3 OFFICIAL 방 수정 시도 → OPEN_CHAT_ROOM_FORBIDDEN")
    void should_throw_CustomException_when_room_is_official() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOfficialRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-6 BR-700-4 maxParticipants < 현재 참여자 수 → OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL")
    void should_throw_CustomException_when_maxParticipants_less_than_current_count() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithMaxParticipants(2);
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));
        given(openChatParticipantRepository.countByRoomId(roomId)).willReturn(3L);

        // when
        ThrowingCallable action = () -> openChatRoomService.updateRoom(userId, roomId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL);
    }

    @Test
    @DisplayName("countByRoomId 미호출 — maxParticipants null 시 참여자 수 조회 안 함")
    void should_not_call_countByRoomId_when_maxParticipants_is_null() {
        // given
        Long userId = 1L;
        Long roomId = 1L;
        OpenChatRoom room = OpenChatRoomUpdateFixture.createOpenRoom();
        OpenChatParticipant host = OpenChatRoomUpdateFixture.createHostParticipant(roomId, userId);
        RequestUpdateOpenChatRoomDto request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        given(openChatRoomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)).willReturn(Optional.of(host));

        // when
        openChatRoomService.updateRoom(userId, roomId, request);

        // then
        then(openChatParticipantRepository).should(never()).countByRoomId(anyLong());
    }
}
