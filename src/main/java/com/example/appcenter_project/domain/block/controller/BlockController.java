package com.example.appcenter_project.domain.block.controller;

import com.example.appcenter_project.domain.block.service.BlockService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/block")
public class BlockController implements BlockApiSpecification {

    private final BlockService blockService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> blockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId) {
        blockService.blockUser(userDetails.getId(), targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> unblockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId) {
        blockService.unblockUser(userDetails.getId(), targetUserId);
        return ResponseEntity.noContent().build();
    }
}
