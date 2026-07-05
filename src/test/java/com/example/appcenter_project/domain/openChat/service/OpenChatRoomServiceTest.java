package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.request.RequestCreatePersonalRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponsePersonalRoomCreatedDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomType;
import com.example.appcenter_project.domain.openChat.fixture.OpenChatRoomFixture;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OpenChatRoomServiceTest {

    @Mock
    OpenChatRoomRepository openChatRoomRepository;

    @Mock
    OpenChatParticipantRepository openChatParticipantRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    OpenChatRoomService openChatRoomService;

    @Test
    @DisplayName("방 생성 성공 — AC-1: isPublic null 전송 시 isPublic=true로 방 생성")
    void should_create_room_with_isPublic_true_when_isPublic_is_null() {
        // given
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequest();
        OpenChatRoom savedRoom = OpenChatRoomFixture.createRoom();
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createRoom(request, 1L);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().isPublic()).isTrue();
    }

    @Test
    @DisplayName("방 생성 성공 — AC-1: password null 전송 시 password=null로 방 생성")
    void should_create_room_with_null_password_when_password_is_null() {
        // given
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequest();
        OpenChatRoom savedRoom = OpenChatRoomFixture.createRoom();
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createRoom(request, 1L);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPassword()).isNull();
    }

    @Test
    @DisplayName("방 생성 성공 — AC-2: isPublic=false + password 설정 시 방에 저장")
    void should_create_room_with_isPublic_false_and_password_when_provided() {
        // given
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequestWithPasswordAndPrivate();
        OpenChatRoom savedRoom = OpenChatRoomFixture.createPrivateRoomWithPassword();
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createRoom(request, 1L);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().isPublic()).isFalse();
        assertThat(captor.getValue().getPassword()).isEqualTo("1234");
    }

    @Test
    @DisplayName("방 생성 성공 — AC-4: password 빈 문자열 전송 시 null로 정규화")
    void should_normalize_blank_password_to_null_when_creating_room() {
        // given
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequestWithBlankPassword();
        OpenChatRoom savedRoom = OpenChatRoomFixture.createRoom();
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createRoom(request, 1L);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPassword()).isNull();
    }

    @Test
    @DisplayName("방 생성 성공 — AC-5: 생성 후 생성자가 host 참여자로 등록됨")
    void should_register_creator_as_host_participant_when_room_created() {
        // given
        Long userId = 1L;
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequest();
        OpenChatRoom savedRoom = OpenChatRoomFixture.createRoom();
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatParticipant> captor = ArgumentCaptor.forClass(OpenChatParticipant.class);

        // when
        openChatRoomService.createRoom(request, userId);

        // then
        then(openChatParticipantRepository).should().save(captor.capture());
        assertThat(captor.getValue().isHost()).isTrue();
    }

    @Test
    @DisplayName("방 생성 성공 — roomType=OPEN으로 저장됨")
    void should_create_room_with_roomType_OPEN() {
        // given
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequest();
        OpenChatRoom savedRoom = OpenChatRoomFixture.createRoom();
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createRoom(request, 1L);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().getRoomType()).isEqualTo(OpenChatRoomType.OPEN);
    }

    @Test
    @DisplayName("방 생성 성공 — scope=DORMITORY 시 creatorDormitory에 dormType 저장")
    void should_save_creatorDormitory_when_scope_is_DORMITORY() {
        // given
        Long userId = 1L;
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequestWithDormitoryScope();
        User mockUser = OpenChatRoomFixture.createUserWithId(userId);
        OpenChatRoom savedRoom = OpenChatRoomFixture.createRoom();
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createRoom(request, userId);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().getCreatorDormitory()).isNotNull();
    }

    @Test
    @DisplayName("방 생성 성공 — createRoom이 roomId를 반환함")
    void should_return_roomId_when_room_created() {
        // given
        RequestCreateOpenChatRoomDto request = OpenChatRoomFixture.createRequest();
        OpenChatRoom savedRoom = OpenChatRoomFixture.createRoomWithId(42L);
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);

        // when
        Long roomId = openChatRoomService.createRoom(request, 1L);

        // then
        assertThat(roomId).isEqualTo(42L);
    }

    @Test
    @DisplayName("개인 채팅방 생성 성공 — AC-6: roomId 반환")
    void should_return_roomId_when_personal_room_created() {
        // given
        Long userId = 1L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequest(2L);
        OpenChatRoom savedRoom = OpenChatRoomFixture.createPersonalRoomWithId(99L);
        given(userRepository.findById(2L)).willReturn(Optional.of(OpenChatRoomFixture.createUserWithId(2L)));
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);

        // when
        ResponsePersonalRoomCreatedDto result = openChatRoomService.createPersonalRoom(userId, request);

        // then
        assertThat(result.getRoomId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("개인 채팅방 생성 성공 — AC-7: password 포함 시 방에 저장")
    void should_create_personal_room_with_password_when_provided() {
        // given
        Long userId = 1L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequestWithPassword(2L, "pass");
        OpenChatRoom savedRoom = OpenChatRoomFixture.createPersonalRoomWithPassword("pass");
        given(userRepository.findById(2L)).willReturn(Optional.of(OpenChatRoomFixture.createUserWithId(2L)));
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createPersonalRoom(userId, request);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("pass");
    }

    @Test
    @DisplayName("개인 채팅방 생성 성공 — AC-8: 생성자(host)와 targetUser 2명이 참여자로 등록됨")
    void should_register_creator_as_host_and_target_as_participant_when_personal_room_created() {
        // given
        Long userId = 1L;
        Long targetUserId = 2L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequest(targetUserId);
        OpenChatRoom savedRoom = OpenChatRoomFixture.createPersonalRoomWithId(99L);
        given(userRepository.findById(targetUserId)).willReturn(Optional.of(OpenChatRoomFixture.createUserWithId(targetUserId)));
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);

        // when
        openChatRoomService.createPersonalRoom(userId, request);

        // then
        then(openChatParticipantRepository).should(times(2)).save(any(OpenChatParticipant.class));
    }

    @Test
    @DisplayName("개인 채팅방 생성 성공 — roomType=PERSONAL로 저장됨")
    void should_create_personal_room_with_roomType_PERSONAL() {
        // given
        Long userId = 1L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequest(2L);
        OpenChatRoom savedRoom = OpenChatRoomFixture.createPersonalRoomWithId(99L);
        given(userRepository.findById(2L)).willReturn(Optional.of(OpenChatRoomFixture.createUserWithId(2L)));
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createPersonalRoom(userId, request);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().getRoomType()).isEqualTo(OpenChatRoomType.PERSONAL);
    }

    @Test
    @DisplayName("개인 채팅방 생성 성공 — isPublic=false 고정 적용")
    void should_create_personal_room_with_isPublic_false() {
        // given
        Long userId = 1L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequest(2L);
        OpenChatRoom savedRoom = OpenChatRoomFixture.createPersonalRoomWithId(99L);
        given(userRepository.findById(2L)).willReturn(Optional.of(OpenChatRoomFixture.createUserWithId(2L)));
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createPersonalRoom(userId, request);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().isPublic()).isFalse();
    }

    @Test
    @DisplayName("개인 채팅방 생성 성공 — maxParticipants=2 고정 적용")
    void should_create_personal_room_with_maxParticipants_2() {
        // given
        Long userId = 1L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequest(2L);
        OpenChatRoom savedRoom = OpenChatRoomFixture.createPersonalRoomWithId(99L);
        given(userRepository.findById(2L)).willReturn(Optional.of(OpenChatRoomFixture.createUserWithId(2L)));
        given(openChatRoomRepository.save(any())).willReturn(savedRoom);
        ArgumentCaptor<OpenChatRoom> captor = ArgumentCaptor.forClass(OpenChatRoom.class);

        // when
        openChatRoomService.createPersonalRoom(userId, request);

        // then
        then(openChatRoomRepository).should().save(captor.capture());
        assertThat(captor.getValue().getMaxParticipants()).isEqualTo(2);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-9: targetUserId가 본인일 때 OPEN_CHAT_SELF_PERSONAL_FORBIDDEN")
    void should_throw_CustomException_when_AC_9_targetUserId_is_self() {
        // given
        Long userId = 1L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequest(userId);

        // when
        ThrowingCallable action = () -> openChatRoomService.createPersonalRoom(userId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_SELF_PERSONAL_FORBIDDEN);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-10: targetUserId에 해당하는 사용자가 없을 때 USER_NOT_FOUND")
    void should_throw_CustomException_when_AC_10_targetUser_not_found() {
        // given
        Long userId = 1L;
        Long targetUserId = 99999L;
        RequestCreatePersonalRoomDto request = OpenChatRoomFixture.createPersonalRoomRequest(targetUserId);
        given(userRepository.findById(targetUserId)).willReturn(Optional.empty());

        // when
        ThrowingCallable action = () -> openChatRoomService.createPersonalRoom(userId, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
