package com.example.appcenter_project.domain.groupOrder.controller;

import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderChatDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderChatDto;
import com.example.appcenter_project.domain.groupOrder.service.GroupOrderChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GroupOrderChatController {

    private final GroupOrderChatService groupOrderChatService;
    private final SimpMessagingTemplate messagingTemplate;

    // GroupOrderChat 전송
    @MessageMapping("/group-order-chat")
    public void sendMessage(
            @Valid RequestGroupOrderChatDto requestGroupOrderChatDto,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        // 세션에서 userId 추출 (WebSocketAuthInterceptor에서 설정됨)
        String userIdStr = (String) headerAccessor.getSessionAttributes().get("userId");
        if (userIdStr == null) {
            throw new RuntimeException("WebSocket authentication required");
        }

        Long userId = Long.parseLong(userIdStr);
        ResponseGroupOrderChatDto responseGroupOrderChatDto = groupOrderChatService.sendGroupOrderChat(userId, requestGroupOrderChatDto);

        // 채팅 웹소켓에 전송
        messagingTemplate.convertAndSend("/sub/group-order-chat-room/" + responseGroupOrderChatDto.getGroupOrderChatRoomId(), responseGroupOrderChatDto);
    }
}
