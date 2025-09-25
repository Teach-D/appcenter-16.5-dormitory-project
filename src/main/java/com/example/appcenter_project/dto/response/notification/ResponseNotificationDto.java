package com.example.appcenter_project.dto.response.notification;

import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ResponseNotificationDto {

    private Long id;
    private Long boardId;
    private String title;
    private String body;
    private String notificationType;
    private boolean isRead;
    private LocalDateTime createdDate;

    public static ResponseNotificationDto entityToDto(UserNotification userNotification) {
        Notification notification = userNotification.getNotification();

        return ResponseNotificationDto.builder()
                .id(notification.getId())
                .boardId(notification.getBoardId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .notificationType(notification.getNotificationType().toValue())
                .isRead(userNotification.isRead())
                .createdDate(notification.getCreatedDate())
                .build();
    }
}
