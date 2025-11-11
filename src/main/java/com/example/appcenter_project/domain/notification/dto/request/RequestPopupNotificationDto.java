package com.example.appcenter_project.domain.notification.dto.request;

import com.example.appcenter_project.domain.notification.entity.PopupNotification;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RequestPopupNotificationDto {

    private String title;
    private String content;
    private String notificationType;
    private LocalDate startDate;
    private LocalDate deadline;
}