package com.example.appcenter_project.dto.request.notification;

import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.enums.user.NotificationType;
import lombok.Getter;

@Getter
public class RequestNotificationDto {

    private String title;
    private String body;
    private String notificationType;
    private Long boardId;

    public static Notification dtoToEntity(RequestNotificationDto requestNotificationDto) {
        return Notification.builder()
                .boardId(requestNotificationDto.getBoardId())
                .title(requestNotificationDto.getTitle())
                .body(requestNotificationDto.getBody())
                .notificationType(NotificationType.from(requestNotificationDto.getNotificationType()))
                .build();
    }
}
