package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.groupOrder.GroupOrderChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-order-chat-rooms")
public class GroupOrderChatRoomController {

    private final GroupOrderChatRoomService groupOrderChatRoomService;

    @PostMapping("/group-order/{groupOrderId}")
    public ResponseEntity<Void> joinChatRoom(@AuthenticationPrincipal SecurityUser user, @PathVariable Long groupOrderId) {
        groupOrderChatRoomService.joinChatRoom(user.getId(), groupOrderId);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ResponseGroupOrderChatRoomDto>> findGroupOrderChatRoom(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(FOUND).body(groupOrderChatRoomService.findGroupOrderChatRoom(user.getId()));
    }

    @PatchMapping("/{chatRoomId}")
    public ResponseEntity<Void> leaveChatRoom(@AuthenticationPrincipal SecurityUser user, @PathVariable Long chatRoomId) {
        groupOrderChatRoomService.leaveChatRoom(user.getId(), chatRoomId);
        return ResponseEntity.status(CREATED).build();
    }
}
