package com.example.appcenter_project.dto.request.notification;

import com.example.appcenter_project.entity.notification.PopupNotification;
import com.example.appcenter_project.enums.user.NotificationType;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class RequestPopupNotificationDto {

    private String title;
    private String content;
    private String notificationType;
    private LocalDate deadline;

    public static PopupNotification dtoToEntity(RequestPopupNotificationDto requestPopupNotificationDto) {
        return PopupNotification.builder()
                .title(requestPopupNotificationDto.getTitle())
                .content(requestPopupNotificationDto.getNotificationType())
                .notificationType(NotificationType.from(requestPopupNotificationDto.getNotificationType()))
                .deadline(requestPopupNotificationDto.getDeadline())
                .build();
    }
}
