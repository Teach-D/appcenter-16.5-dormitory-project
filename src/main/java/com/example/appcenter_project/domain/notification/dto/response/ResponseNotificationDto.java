package com.example.appcenter_project.domain.notification.dto.response;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
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
    private String apiType;
    private boolean isRead;
    private LocalDateTime createdDate;

    public static ResponseNotificationDto from(UserNotification userNotification) {
        Notification notification = userNotification.getNotification();

        return ResponseNotificationDto.builder()
                .id(userNotification.getId())
                .boardId(notification.getBoardId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .notificationType(notification.getNotificationType().toValue())
                .apiType(String.valueOf(notification.getApiType()))
                .isRead(userNotification.isRead())
                .createdDate(userNotification.getCreatedDate())
                .build();
    }
}
