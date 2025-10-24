package com.example.appcenter_project.domain.notification.entity;

import com.example.appcenter_project.domain.notification.dto.request.RequestNotificationDto;
import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
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

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    private ApiType apiType;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
    private List<UserNotification> userNotifications = new ArrayList<>();

    @Builder
    public Notification(Long boardId, String title, String body, NotificationType notificationType, ApiType apiType, List<UserNotification> userNotifications) {
        this.boardId = boardId;
        this.title = title;
        this.body = body;
        this.notificationType = notificationType;
        this.apiType = apiType;
        this.userNotifications = userNotifications;
    }

    public static Notification from(RequestNotificationDto dto) {
        return Notification.builder()
                .boardId(dto.getBoardId())
                .title(dto.getTitle())
                .body(dto.getBody())
                .notificationType(NotificationType.from(dto.getNotificationType()))
                .apiType(ApiType.NOTIFICATION)
                .build();
    }

    public static Notification of(String title, String body, NotificationType notificationType, ApiType apiType, Long boardId) {
        return Notification.builder()
                .title(title)
                .body(body)
                .notificationType(notificationType)
                .apiType(apiType)
                .boardId(boardId)
                .build();

    }


    public void update(RequestNotificationDto requestNotificationDto) {
        boardId = requestNotificationDto.getBoardId();
        title = requestNotificationDto.getTitle();
        body = requestNotificationDto.getBody();
        notificationType = NotificationType.from(requestNotificationDto.getNotificationType());
    }
}
