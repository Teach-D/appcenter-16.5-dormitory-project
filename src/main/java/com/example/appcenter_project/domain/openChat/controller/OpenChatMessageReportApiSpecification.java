package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestReportOpenChatMessageDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "OpenChat Report", description = "오픈채팅 메시지 신고 API")
public interface OpenChatMessageReportApiSpecification {

    @Operation(summary = "오픈채팅 메시지 신고",
            description = "방 참여자가 타인이 보낸 메시지를 신고. 자동 제재 X, 접수만")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "신고 접수 성공"),
            @ApiResponse(responseCode = "400", description = "사유 누락 / 본인 메시지 / 신고 불가 메시지", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content),
            @ApiResponse(responseCode = "403", description = "해당 방의 참여자가 아님", content = @Content),
            @ApiResponse(responseCode = "404", description = "메시지 또는 사용자를 찾을 수 없음", content = @Content)
    })
    ResponseEntity<Void> reportMessage(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "신고 대상 메시지 ID", required = true, example = "1")
            @PathVariable Long messageId,
            @Parameter(description = "신고 요청 데이터", required = true)
            @Valid @RequestBody RequestReportOpenChatMessageDto request);
}