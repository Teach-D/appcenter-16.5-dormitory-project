package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseLeaveOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDetailDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;

import java.time.LocalDateTime;
import java.util.List;

public class OpenChatRoomFixture {

    public static OpenChatRoom createRoom() {
        return OpenChatRoom.create("테스트 채팅방", "설명", OpenChatRoomScope.ALL, 10, 1L, null, false);
    }

    public static OpenChatRoom createRoomWithScope(OpenChatRoomScope scope) {
        String dormitory = scope == OpenChatRoomScope.DORMITORY ? "SAMSUNG" : null;
        return OpenChatRoom.create("테스트 채팅방", "설명", scope, 10, 1L, dormitory, false);
    }

    public static OpenChatRoom createOfficialRoom() {
        return OpenChatRoom.create("공식 방", "설명", OpenChatRoomScope.ALL, 100, null, null, true);
    }

    public static OpenChatRoom createFullRoom() {
        return OpenChatRoom.create("꽉 찬 방", "설명", OpenChatRoomScope.ALL, 2, 1L, null, false);
    }

    public static OpenChatParticipant createParticipant(Long userId) {
        return OpenChatParticipant.create(1L, userId, LocalDateTime.now());
    }

    public static OpenChatParticipant createParticipantWithJoinedAt(Long userId, LocalDateTime joinedAt) {
        return OpenChatParticipant.create(1L, userId, joinedAt);
    }

    public static RequestCreateOpenChatRoomDto createRequest() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("테스트 채팅방")
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .maxParticipants(10)
                .build();
    }

    public static RequestCreateOpenChatRoomDto createRequestWithDormitoryScope() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("테스트 채팅방")
                .description("설명")
                .scope(OpenChatRoomScope.DORMITORY)
                .maxParticipants(10)
                .build();
    }

    public static RequestCreateOpenChatRoomDto createRequestWithBlankName() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("   ")
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .maxParticipants(10)
                .build();
    }

    public static RequestCreateOpenChatRoomDto createRequestWithLongName() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("a".repeat(31))
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .maxParticipants(10)
                .build();
    }

    public static RequestCreateOpenChatRoomDto createRequestWithLongDescription() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("테스트 채팅방")
                .description("a".repeat(101))
                .scope(OpenChatRoomScope.ALL)
                .maxParticipants(10)
                .build();
    }

    public static RequestCreateOpenChatRoomDto createRequestWithMinParticipants() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("테스트 채팅방")
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .maxParticipants(1)
                .build();
    }

    public static RequestCreateOpenChatRoomDto createRequestWithMaxParticipantsExceeded() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("테스트 채팅방")
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .maxParticipants(101)
                .build();
    }

    public static RequestCreateOpenChatRoomDto createRequestWithNullScope() {
        return RequestCreateOpenChatRoomDto.builder()
                .name("테스트 채팅방")
                .description("설명")
                .scope(null)
                .maxParticipants(10)
                .build();
    }

    public static ResponseOpenChatRoomDetailDto createDetailResponse() {
        return ResponseOpenChatRoomDetailDto.builder()
                .roomId(1L)
                .name("테스트 채팅방")
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .currentParticipants(1)
                .maxParticipants(10)
                .isOfficial(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ResponseOpenChatRoomDto createRoomResponse() {
        return ResponseOpenChatRoomDto.builder()
                .roomId(1L)
                .name("테스트 채팅방")
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .currentParticipants(1)
                .maxParticipants(10)
                .isJoined(false)
                .lastMessageAt(null)
                .lastMessage(null)
                .unreadCount(0)
                .build();
    }

    public static ResponseLeaveOpenChatRoomDto createLeaveResponse(boolean roomDeleted) {
        return ResponseLeaveOpenChatRoomDto.builder()
                .roomDeleted(roomDeleted)
                .build();
    }

    public static List<ResponseOpenChatRoomDto> createRoomResponseList() {
        return List.of(createRoomResponse());
    }
}
