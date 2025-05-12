package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.groupOrder.GroupOrderCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-order-comments")
public class GroupOrderCommentController {

    private final GroupOrderCommentService groupOrderCommentService;

    @PostMapping
    public ResponseEntity<ResponseGroupOrderCommentDto> saveGroupOrderComment(@AuthenticationPrincipal SecurityUser user, @RequestBody RequestGroupOrderCommentDto requestGroupOrderCommentDto) {
        return ResponseEntity.status(CREATED).body(groupOrderCommentService.saveGroupOrderComment(user.getId(), requestGroupOrderCommentDto));
    }

    @GetMapping("/group-order/{groupOrderId}")
    public ResponseEntity<List<ResponseGroupOrderCommentDto>> findGroupOrderComment(@AuthenticationPrincipal SecurityUser user, @PathVariable Long groupOrderId) {
        return ResponseEntity.status(FOUND).body(groupOrderCommentService.findGroupOrderComment(user.getId(), groupOrderId));
    }
}
