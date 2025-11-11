package com.example.appcenter_project.domain.notification.dto.request;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import lombok.Getter;

@Getter
public class RequestNotificationDto {

    private String title;
    private String body;
    private String notificationType;
    private Long boardId;
}