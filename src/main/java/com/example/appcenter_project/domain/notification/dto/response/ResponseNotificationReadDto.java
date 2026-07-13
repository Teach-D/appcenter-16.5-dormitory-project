package com.example.appcenter_project.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseNotificationReadDto {

    private boolean success;
    private String message;

    public static ResponseNotificationReadDto success() {
        return ResponseNotificationReadDto.builder()
                .success(true)
                .message("성공적으로 읽음 처리되었습니다.")
                .build();
    }
}
