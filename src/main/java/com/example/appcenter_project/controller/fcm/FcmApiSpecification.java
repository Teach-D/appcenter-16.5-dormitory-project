package com.example.appcenter_project.controller.fcm;

import com.example.appcenter_project.dto.request.fcm.RequestFcmMessageDto;
import com.example.appcenter_project.dto.request.fcm.RequestFcmTokenDto;
import com.example.appcenter_project.dto.response.fcm.ResponseFcmMessageDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "FCM", description = "Firebase Cloud Messaging 관련 API")
public interface FcmApiSpecification {

    @Operation(
            summary = "FCM 토큰 저장",
            description = "로그인한 사용자의 FCM 토큰을 저장합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 저장 성공"),
                    @ApiResponse(responseCode = "404", description = "유저 없음 (USER_NOT_FOUND)", content = @Content())
            }
    )
    ResponseEntity<Void> saveToken(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody @Parameter(description = "FCM 토큰 요청 DTO", required = true)
            RequestFcmTokenDto requestDto
    );

    @Operation(
            summary = "FCM 메시지 발송",
            description = "지정한 targetToken으로 푸시 알림을 발송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메시지 발송 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseFcmMessageDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (토큰/제목/내용 누락)", content = @Content()),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류 (FCM 발송 실패)", content = @Content())
            }
    )
    ResponseEntity<ResponseFcmMessageDto> sendMessage(
            @RequestBody @Parameter(description = "FCM 메시지 요청 DTO", required = true)
            RequestFcmMessageDto requestDto
    );
}
