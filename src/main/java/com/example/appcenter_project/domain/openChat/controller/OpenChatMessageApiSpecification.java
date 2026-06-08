package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageListDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "OpenChat Message", description = "오픈 채팅 메시지 API")
public interface OpenChatMessageApiSpecification {

    @Operation(summary = "채팅 메시지 목록 조회", description = "커서 기반 페이지네이션으로 채팅 메시지를 조회한다")
    ResponseEntity<ResponseOpenChatMessageListDto> getMessages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "30") int size);
}
