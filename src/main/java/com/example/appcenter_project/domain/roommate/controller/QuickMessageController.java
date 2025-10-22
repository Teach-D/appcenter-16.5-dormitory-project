package com.example.appcenter_project.domain.roommate.controller;


import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.notification.service.QuickMessageService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quick-message")
@RequiredArgsConstructor
public class QuickMessageController {

    private final QuickMessageService quickMessageService;

    // 퀵메시지 전송
    @PostMapping
    public ResponseEntity<String> sendQuickMessage(
            @AuthenticationPrincipal
            @Parameter(description = "로그인 사용자", required = true)
            CustomUserDetails userDetails,

            @RequestParam
            @Parameter(description = "보낼 메시지 내용", example = "오늘 저녁 뭐 먹을까?", required = true)
            String message
    ) {
        quickMessageService.sendQuickMessageToMyRoommate(userDetails.getId(), message);
        return ResponseEntity.status(HttpStatus.CREATED).body("퀵메시지 전송 완료");
    }
}