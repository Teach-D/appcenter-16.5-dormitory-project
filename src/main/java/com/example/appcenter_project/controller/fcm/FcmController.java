package com.example.appcenter_project.controller.fcm;

import com.example.appcenter_project.dto.request.fcm.RequestFcmMessageDto;
import com.example.appcenter_project.dto.request.fcm.RequestFcmTokenDto;
import com.example.appcenter_project.dto.response.fcm.ResponseFcmMessageDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import com.example.appcenter_project.service.fcm.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController implements FcmApiSpecification{

    private final FcmTokenService fcmTokenService;
    private final FcmMessageService fcmMessageService;

    @PostMapping("/token")
    public ResponseEntity<Void> saveToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestFcmTokenDto requestDto
    ) {
        fcmTokenService.saveToken(userDetails, requestDto.getFcmToken());
        return ResponseEntity.ok().build();
    }

    // 전체 사용자에게 알림 전송 (user_id가 NULL인 토큰도 포함)
    @PostMapping("/send/all")
    public ResponseEntity<ResponseFcmMessageDto> sendMessageToAllUsers(
            @RequestBody RequestFcmMessageDto requestDto
    ) {
        ResponseFcmMessageDto response = fcmMessageService.sendNotificationToAllUsers(
                requestDto.getTitle(),
                requestDto.getBody()
        );
        return ResponseEntity.ok(response);
    }



/*    @PostMapping("/send")
    public ResponseEntity<ResponseFcmMessageDto> sendMessage(
            @RequestBody RequestFcmMessageDto requestDto
    ) {
        String messageId = fcmMessageService.sendNotification(
                requestDto.getTargetToken(),
                requestDto.getTitle(),
                requestDto.getBody()
        );

        return ResponseEntity.ok(
                ResponseFcmMessageDto.builder()
                        .messageId(messageId)
                        .status("SUCCESS")
                        .build()
        );
    }*/
}
