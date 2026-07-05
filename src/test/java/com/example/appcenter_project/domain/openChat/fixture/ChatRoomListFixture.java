package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseChatRoomListDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.enums.ChatCategory;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomType;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomListFixture {

    public static ResponseOpenChatRoomDto createOpenChatRoomResponse() {
        return ResponseOpenChatRoomDto.builder()
                .roomId(1L)
                .name("공부방")
                .description("조용히 공부해요")
                .scope(OpenChatRoomScope.ALL)
                .roomType(OpenChatRoomType.OPEN)
                .chatCategory(ChatCategory.OPEN_CHAT)
                .isPublic(true)
                .hasPassword(false)
                .currentParticipants(5)
                .maxParticipants(30)
                .isJoined(false)
                .lastMessageAt(LocalDateTime.of(2026, 7, 5, 12, 0, 0))
                .lastMessage("오늘 스터디 있어요")
                .unreadCount(0)
                .build();
    }

    public static ResponseOpenChatRoomDto createOpenChatRoomResponseWithName(String name) {
        return ResponseOpenChatRoomDto.builder()
                .roomId(1L)
                .name(name)
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .roomType(OpenChatRoomType.OPEN)
                .chatCategory(ChatCategory.OPEN_CHAT)
                .isPublic(true)
                .hasPassword(false)
                .currentParticipants(1)
                .maxParticipants(30)
                .isJoined(false)
                .lastMessageAt(null)
                .lastMessage(null)
                .unreadCount(0)
                .build();
    }

    public static ResponseOpenChatRoomDto createOpenChatRoomResponseJoined() {
        return ResponseOpenChatRoomDto.builder()
                .roomId(2L)
                .name("내 채팅방")
                .description("설명")
                .scope(OpenChatRoomScope.ALL)
                .roomType(OpenChatRoomType.OPEN)
                .chatCategory(ChatCategory.OPEN_CHAT)
                .isPublic(true)
                .hasPassword(false)
                .currentParticipants(3)
                .maxParticipants(50)
                .isJoined(true)
                .lastMessageAt(LocalDateTime.of(2026, 7, 5, 10, 0, 0))
                .lastMessage("안녕")
                .unreadCount(3)
                .build();
    }

    public static ResponseOpenChatRoomDto createRoommateRoomResponse() {
        return ResponseOpenChatRoomDto.builder()
                .roomId(10L)
                .name("김철수")
                .description(null)
                .scope(null)
                .roomType(null)
                .chatCategory(ChatCategory.ROOMMATE)
                .isPublic(false)
                .hasPassword(false)
                .currentParticipants(2)
                .maxParticipants(2)
                .isJoined(true)
                .lastMessageAt(LocalDateTime.of(2026, 7, 5, 13, 20, 0))
                .lastMessage("안녕하세요!")
                .unreadCount(2)
                .build();
    }

    public static ResponseOpenChatRoomDto createRoommateRoomResponseWithOpponentName(String opponentName) {
        return ResponseOpenChatRoomDto.builder()
                .roomId(10L)
                .name(opponentName)
                .description(null)
                .scope(null)
                .roomType(null)
                .chatCategory(ChatCategory.ROOMMATE)
                .isPublic(false)
                .hasPassword(false)
                .currentParticipants(2)
                .maxParticipants(2)
                .isJoined(true)
                .lastMessageAt(null)
                .lastMessage(null)
                .unreadCount(0)
                .build();
    }

    public static ResponseChatRoomListDto createChatRoomListDto(
            List<ResponseOpenChatRoomDto> content, int totalUnreadCount) {
        return ResponseChatRoomListDto.builder()
                .content(content)
                .totalElements(content.size())
                .totalPages(1)
                .pageNumber(0)
                .pageSize(20)
                .totalUnreadCount(totalUnreadCount)
                .build();
    }

    public static ResponseChatRoomListDto createAllTabResponse() {
        List<ResponseOpenChatRoomDto> content = List.of(createOpenChatRoomResponse());
        return createChatRoomListDto(content, 0);
    }

    public static ResponseChatRoomListDto createMyTabResponse() {
        List<ResponseOpenChatRoomDto> content = List.of(
                createRoommateRoomResponse(),
                createOpenChatRoomResponseJoined()
        );
        return createChatRoomListDto(content, 5);
    }

    public static ResponseChatRoomListDto createEmptyResponse() {
        return createChatRoomListDto(List.of(), 0);
    }
}
