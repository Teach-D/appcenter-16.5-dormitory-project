package com.example.appcenter_project.domain.groupOrder.controller;

import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderCommentDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.domain.groupOrder.service.GroupOrderCommentService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-order-comments")
public class GroupOrderCommentController implements GroupOrderCommentApiSpecification {

    private final GroupOrderCommentService groupOrderCommentService;

    @PostMapping
    public ResponseEntity<ResponseGroupOrderCommentDto> saveGroupOrderComment(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestGroupOrderCommentDto requestGroupOrderCommentDto) {
        return ResponseEntity.status(CREATED).body(groupOrderCommentService.saveGroupOrderComment(user.getId(), requestGroupOrderCommentDto));
    }

    @DeleteMapping("/{groupOrderCommentId}")
    public ResponseEntity<Void> deleteGroupOrderComment(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderCommentId) {
        groupOrderCommentService.deleteGroupOrderComment(user.getId(), groupOrderCommentId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

}