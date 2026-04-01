package com.example.appcenter_project.domain.notification.dto.request;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RequestNotificationDto {

    private String title;
    private String body;

    @Schema(
            description = "알림 타입 (영문 또는 한글 모두 허용)",
            allowableValues = {"ROOMMATE", "GROUP_ORDER", "DORMITORY", "UNI_DORM", "SUPPORTERS", "COMPLAINT", "COUPON", "CHAT"},
            example = "UNI_DORM"
    )
    private String notificationType;
}