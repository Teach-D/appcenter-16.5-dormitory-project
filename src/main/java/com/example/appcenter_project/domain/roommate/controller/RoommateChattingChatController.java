package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateChatDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateChatDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.roommate.service.RoommateChattingChatService;
import io.micrometer.core.annotation.Counted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/roommate/chat")
public class RoommateChattingChatController implements RoommateChatApiSpecification {

    private final RoommateChattingChatService chatService;

    // 채팅 보내기
    @Counted("roommate.chatting.send")
    @PostMapping
    public ResponseEntity<ResponseRoommateChatDto> sendChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestRoommateChatDto request
    ) {
        Long userId = userDetails.getId();
        ResponseRoommateChatDto response = chatService.sendChat(userId, request);
        return ResponseEntity.ok(response);
    }

    // 채팅 내역 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<List<ResponseRoommateChatDto>> getChatList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        Long userId = userDetails.getId();
        List<ResponseRoommateChatDto> chatList = chatService.getChatList(userId, roomId);
        return ResponseEntity.ok(chatList);
    }

    // 읽음 처리
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        Long userId = userDetails.getId();
        chatService.markAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    // WebSocket 방식 채팅 보내기
    @MessageMapping("/roommate/socketchat")
    public void sendChatViaWebSocket(
            @Valid RequestRoommateChatDto request,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // 세션에서 userId 추출 (WebSocketAuthInterceptor에서 설정됨)
        String userIdStr = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userIdStr == null) {
            throw new RuntimeException("WebSocket authentication required");
        }

        Long userId = Long.parseLong(userIdStr);
        String sessionId = headerAccessor.getSessionId();

        log.info("📤 [WebSocket 채팅 전송] userId: {}, sessionId: {}, roomId: {}, content: {}",
                userId, sessionId, request.getRoommateChattingRoomId(), request.getContent());

        chatService.sendChat(userId, request);
    }


}
