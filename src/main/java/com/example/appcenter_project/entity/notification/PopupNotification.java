package com.example.appcenter_project.entity.notification;

import com.example.appcenter_project.dto.request.notification.RequestPopupNotificationDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.enums.user.NotificationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class PopupNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private LocalDate deadline;

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<Image> images = new ArrayList<>();

    @Builder
    public PopupNotification(String title, String content, NotificationType notificationType, LocalDate deadline) {
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.deadline = deadline;
    }

    public void update(RequestPopupNotificationDto requestPopupNotificationDto) {
        this.title = requestPopupNotificationDto.getTitle();
        this.content = requestPopupNotificationDto.getContent();
        this.notificationType = NotificationType.valueOf(requestPopupNotificationDto.getNotificationType());
        this.deadline = LocalDate.now();
    }
}
