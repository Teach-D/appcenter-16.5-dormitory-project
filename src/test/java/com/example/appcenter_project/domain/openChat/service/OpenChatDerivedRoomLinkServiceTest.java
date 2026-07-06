package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.fixture.OpenChatDerivedRoomLinkFixture;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OpenChatDerivedRoomLinkServiceTest {

    @Mock
    OpenChatRoomRepository openChatRoomRepository;

    @Mock
    OpenChatParticipantRepository openChatParticipantRepository;

    @Mock
    OpenChatMessageRepository openChatMessageRepository;

    @Mock
    OpenChatMessageService openChatMessageService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    OpenChatRoomService openChatRoomService;

    @Test
    @DisplayName("CustomException 발생 — BR-674 originRoom이 존재하지 않을 때 OPEN_CHAT_ROOM_NOT_FOUND")
    void should_throw_CustomException_when_origin_room_not_found() {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        given(openChatRoomRepository.findById(1L)).willReturn(Optional.empty());

        // when
        ThrowingCallable action = () -> openChatRoomService.createDerivedRoom(7L, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("CustomException 발생 — BR-674 요청자가 originRoom 참여자가 아닐 때 OPEN_CHAT_PARTICIPANT_NOT_FOUND")
    void should_throw_CustomException_when_requester_is_not_origin_room_participant() {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        OpenChatRoom originRoom = mock(OpenChatRoom.class);
        given(openChatRoomRepository.findById(1L)).willReturn(Optional.of(originRoom));
        given(openChatParticipantRepository.existsByRoomIdAndUserId(1L, 7L)).willReturn(false);

        // when
        ThrowingCallable action = () -> openChatRoomService.createDerivedRoom(7L, request);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND);
    }

    @Test
    @DisplayName("sendRoomLinkMessage 호출 — BR-674 파생 방 생성 성공 시 ROOM_LINK 메시지 전송")
    void should_call_sendRoomLinkMessage_when_derived_room_created_successfully() {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        OpenChatRoom originRoom = mock(OpenChatRoom.class);
        given(openChatRoomRepository.findById(1L)).willReturn(Optional.of(originRoom));
        given(openChatParticipantRepository.existsByRoomIdAndUserId(1L, 7L)).willReturn(true);
        OpenChatRoom savedDerivedRoom = mock(OpenChatRoom.class);
        given(savedDerivedRoom.getId()).willReturn(42L);
        given(openChatRoomRepository.save(any())).willReturn(savedDerivedRoom);

        // when
        openChatRoomService.createDerivedRoom(7L, request);

        // then
        // TODO: sendRoomLinkMessage가 OpenChatMessageService에 추가되면 컴파일 오류 해소 — 의도된 RED
        then(openChatMessageService).should(atLeastOnce()).sendRoomLinkMessage(
                eq(1L), eq(7L), anyLong(), eq("토론방"), eq("자유롭게 토론해요"), eq(30));
    }

    @Test
    @DisplayName("sendRoomLinkMessage 미호출 — BR-674 originRoom 없을 때 ROOM_LINK 메시지 저장 안 됨")
    void should_not_call_sendRoomLinkMessage_when_origin_room_not_found() {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        given(openChatRoomRepository.findById(1L)).willReturn(Optional.empty());

        // when
        ThrowingCallable action = () -> openChatRoomService.createDerivedRoom(7L, request);
        try { action.call(); } catch (Throwable ignored) {}

        // then
        // TODO: sendRoomLinkMessage가 OpenChatMessageService에 추가되면 컴파일 오류 해소 — 의도된 RED
        then(openChatMessageService).should(never()).sendRoomLinkMessage(
                anyLong(), anyLong(), anyLong(), anyString(), any(), anyInt());
    }

    @Test
    @DisplayName("sendRoomLinkMessage 미호출 — BR-674 참여자 아닐 때 ROOM_LINK 메시지 저장 안 됨")
    void should_not_call_sendRoomLinkMessage_when_requester_is_not_participant() {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        OpenChatRoom originRoom = mock(OpenChatRoom.class);
        given(openChatRoomRepository.findById(1L)).willReturn(Optional.of(originRoom));
        given(openChatParticipantRepository.existsByRoomIdAndUserId(1L, 7L)).willReturn(false);

        // when
        ThrowingCallable action = () -> openChatRoomService.createDerivedRoom(7L, request);
        try { action.call(); } catch (Throwable ignored) {}

        // then
        // TODO: sendRoomLinkMessage가 OpenChatMessageService에 추가되면 컴파일 오류 해소 — 의도된 RED
        then(openChatMessageService).should(never()).sendRoomLinkMessage(
                anyLong(), anyLong(), anyLong(), anyString(), any(), anyInt());
    }
}
