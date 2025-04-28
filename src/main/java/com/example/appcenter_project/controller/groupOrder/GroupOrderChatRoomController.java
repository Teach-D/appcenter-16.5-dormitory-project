package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDetailDto;
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
    public ResponseEntity<List<ResponseGroupOrderChatRoomDto>> findGroupOrderChatRoomList(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(FOUND).body(groupOrderChatRoomService.findGroupOrderChatRoomList(user.getId()));
    }

    @GetMapping("/{groupOrderChatRoomId}")
    public ResponseEntity<ResponseGroupOrderChatRoomDetailDto> findGroupOrderChatRoom(@PathVariable Long groupOrderChatRoomId) {
        return ResponseEntity.status(FOUND).body(groupOrderChatRoomService.findGroupOrderChatRoom(groupOrderChatRoomId));
    }

    @GetMapping("/group-order/{groupOrderId}")
    public ResponseEntity<ResponseGroupOrderChatRoomDetailDto> findGroupOrderChatRoomByGroupOrder(@PathVariable Long groupOrderId) {
        return ResponseEntity.status(FOUND).body(groupOrderChatRoomService.findGroupOrderChatRoomByGroupOrder(groupOrderId));
    }

    @PatchMapping("/{chatRoomId}")
    public ResponseEntity<Void> leaveChatRoom(@AuthenticationPrincipal SecurityUser user, @PathVariable Long chatRoomId) {
        groupOrderChatRoomService.leaveChatRoom(user.getId(), chatRoomId);
        return ResponseEntity.status(CREATED).build();
    }
}
