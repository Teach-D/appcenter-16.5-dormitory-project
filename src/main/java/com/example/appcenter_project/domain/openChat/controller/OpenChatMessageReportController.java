package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestReportOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.service.OpenChatMessageReportService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-chat-rooms")
public class OpenChatMessageReportController implements OpenChatMessageReportApiSpecification {

    private final OpenChatMessageReportService reportService;

    @PostMapping("/messages/{messageId}/reports")
    public ResponseEntity<Void> reportMessage(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long messageId,
            @Valid @RequestBody RequestReportOpenChatMessageDto request) {
        reportService.reportMessage(user.getId(), messageId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}