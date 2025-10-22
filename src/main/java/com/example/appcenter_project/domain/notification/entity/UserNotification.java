package com.example.appcenter_project.domain.notification.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    private Notification notification;
    private boolean isRead = false;

    @Builder
    public UserNotification(User user, Notification notification, boolean isRead) {
        this.user = user;
        this.notification = notification;
        this.isRead = isRead;
    }

    public static UserNotification of(User user, Notification notification) {
        return UserNotification.builder()
                .user(user)
                .notification(notification)
                .build();
    }


    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
