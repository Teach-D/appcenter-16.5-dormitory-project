package com.example.appcenter_project.entity.notification;

import com.example.appcenter_project.dto.request.notification.RequestNotificationDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.enums.user.NotificationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long boardId;
    private String title;
    private String body;
    private NotificationType notificationType;

    @OneToMany(mappedBy = "notification")
    private List<UserNotification> userNotifications = new ArrayList<>();

    @Builder
    public Notification(Long boardId, String title, String body, NotificationType notificationType, List<UserNotification> userNotifications) {
        this.boardId = boardId;
        this.title = title;
        this.body = body;
        this.notificationType = notificationType;
        this.userNotifications = userNotifications;
    }

    public void update(RequestNotificationDto requestNotificationDto) {
        boardId = requestNotificationDto.getBoardId();
        title = requestNotificationDto.getTitle();
        body = requestNotificationDto.getBody();
        notificationType = NotificationType.from(requestNotificationDto.getNotificationType());
    }
}
