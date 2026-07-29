package com.example.appcenter_project.domain.block.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.appcenter_project.global.security.CustomUserDetails;

@Tag(name = "Block", description = "사용자 차단 API")
public interface BlockApiSpecification {

    @Operation(summary = "사용자 차단", description = "targetUserId를 차단한다. 차단 후 차단당한 사용자는 1대1 채팅방 생성 및 메시지 전송이 불가하다.")
    ResponseEntity<Void> blockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId);

    @Operation(summary = "사용자 차단 해제", description = "자신이 차단한 targetUserId를 차단 해제한다.")
    ResponseEntity<Void> unblockUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId);
}
