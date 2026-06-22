package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageListDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "OpenChat Message", description = "오픈 채팅 메시지 API")
public interface OpenChatMessageApiSpecification {

    @Operation(summary = "채팅 메시지 목록 조회", description = "커서 기반 페이지네이션으로 채팅 메시지를 조회한다")
    ResponseEntity<ResponseOpenChatMessageListDto> getMessages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(defaultValue = "30") int size,
            HttpServletRequest request);

    @Operation(summary = "이미지 메시지 전송", description = "채팅방에 이미지를 전송한다. 저장 완료 후 WebSocket으로 전체 참여자에게 브로드캐스트된다.")
    ResponseEntity<ResponseOpenChatMessageDto> sendImageMessage(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestPart(value = "images", required = false)
            @Parameter(description = "전송할 이미지 파일 목록 (jpg/jpeg/png/gif/webp, 최대 5개)", required = false)
            List<MultipartFile> images,
            HttpServletRequest request);
}
