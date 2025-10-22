package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Quick Message API", description = "룸메이트 간 퀵메시지 관련 API")
public interface QuickMessageApiSpecification {

    @Operation(
            summary = "내 룸메이트에게 퀵메시지 전송",
            description = """
                    로그인한 사용자가 자신의 룸메이트에게 짧은 메시지를 전송합니다.  
                    룸메이트 정보는 MyRoommate 테이블에서 자동 조회되며,  
                    내부적으로 Notification 및 UserNotification이 생성되고,  
                    룸메이트에게 FCM 푸시 알림이 전송됩니다.  
                    (예: “홍길동님이 퀵메시지를 보냈어요!”)
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "전송 성공",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자 또는 룸메이트 정보를 찾을 수 없음"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "FCM 전송 실패 또는 서버 내부 오류"
                    )
            }
    )
    ResponseEntity<String> sendQuickMessage(
            @AuthenticationPrincipal
            @Parameter(description = "로그인 사용자", required = true)
            CustomUserDetails userDetails,

            @RequestParam
            @Parameter(description = "보낼 메시지 내용", example = "오늘 늦을 것 같아요", required = true)
            String message
    );
}
