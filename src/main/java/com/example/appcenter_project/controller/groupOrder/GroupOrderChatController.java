package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderChatDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChat;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.groupOrder.GroupOrderChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GroupOrderChatController {

    private final GroupOrderChatService groupOrderChatService;
    private final SimpMessagingTemplate messagingTemplate;

    // GroupOrderChat 전송
    @MessageMapping("/groupOrderChat")
    public void sendMessage(@AuthenticationPrincipal SecurityUser user, RequestGroupOrderChatDto requestGroupOrderChatDto) {
        ResponseGroupOrderChatDto responseGroupOrderChatDto = groupOrderChatService.sendGroupOrderChat(user.getId(), requestGroupOrderChatDto);

        // 채팅 웹소켓에 전송
        messagingTemplate.convertAndSend("/sub/groupOrderChatRoom/" + responseGroupOrderChatDto.getGroupOrderChatRoomId(), responseGroupOrderChatDto);
    }
}
