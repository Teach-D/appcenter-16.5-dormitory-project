package com.example.appcenter_project.entity.notification;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class  UserNotification extends BaseTimeEntity {

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

    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
