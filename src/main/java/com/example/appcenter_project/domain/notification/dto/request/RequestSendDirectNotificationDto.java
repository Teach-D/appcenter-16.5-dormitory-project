package com.example.appcenter_project.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RequestSendDirectNotificationDto {

    private String studentNumber;
    private String title;
    private String content;

    @Schema(
            description = "알림 타입 (미입력 시 UNI_DORM 기본값)",
            allowableValues = {"ROOMMATE", "GROUP_ORDER", "DORMITORY", "UNI_DORM", "SUPPORTERS", "COMPLAINT", "COUPON", "CHAT"},
            example = "UNI_DORM"
    )
    private String notificationType;
}
