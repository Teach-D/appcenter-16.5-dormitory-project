package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateChatDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateChatDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.roommate.RoommateChattingChatService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roommate/chat")
public class RoommateChattingChatController implements RoommateChatApiSpecification {

    private final RoommateChattingChatService chatService;

    // 채팅 보내기
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid RequestRoommateChatDto request
    ) {
        Long userId = userDetails.getId();
        chatService.sendChat(userId, request);
    }


}
