package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.groupOrder.GroupOrderCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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