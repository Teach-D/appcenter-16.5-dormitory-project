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
    private LocalDate startDate;
    private LocalDate deadline;

    public static PopupNotification dtoToEntity(RequestPopupNotificationDto requestPopupNotificationDto) {
        return PopupNotification.builder()
                .title(requestPopupNotificationDto.getTitle())
                .content(requestPopupNotificationDto.getContent())
                .notificationType(NotificationType.from(requestPopupNotificationDto.getNotificationType()))
                .startDate(requestPopupNotificationDto.getStartDate())
                .deadline(requestPopupNotificationDto.getDeadline())
                .build();
    }
}
