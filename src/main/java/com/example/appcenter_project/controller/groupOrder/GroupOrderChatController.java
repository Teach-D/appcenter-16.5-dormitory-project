package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.groupOrder.GroupOrderChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-order-chat-rooms")
public class GroupOrderChatController {

    private final GroupOrderChatRoomService groupOrderChatRoomService;

    @PostMapping("/{chatRoomId}")
    public ResponseEntity<Void> joinChatRoom(@AuthenticationPrincipal SecurityUser user, @PathVariable Long chatRoomId) {
        groupOrderChatRoomService.joinChatRoom(user.getId(), chatRoomId);
        return ResponseEntity.status(CREATED).build();
    }

    @PatchMapping("/{chatRoomId}")
    public ResponseEntity<Void> leaveChatRoom(@AuthenticationPrincipal SecurityUser user, @PathVariable Long chatRoomId) {
        groupOrderChatRoomService.leaveChatRoom(user.getId(), chatRoomId);
        return ResponseEntity.status(CREATED).build();
    }
}
