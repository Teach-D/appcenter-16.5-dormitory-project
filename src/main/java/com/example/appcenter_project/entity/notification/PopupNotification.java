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

    private LocalDate startDate;

    private LocalDate deadline;

    @Builder
    public PopupNotification(String title, String content, NotificationType notificationType, LocalDate startDate, LocalDate deadline) {
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.startDate = startDate;
        this.deadline = deadline;
    }

    public void update(RequestPopupNotificationDto requestPopupNotificationDto) {
        this.title = requestPopupNotificationDto.getTitle();
        this.content = requestPopupNotificationDto.getContent();
        this.notificationType = NotificationType.from(requestPopupNotificationDto.getNotificationType());
        this.startDate = requestPopupNotificationDto.getStartDate();
        this.deadline = requestPopupNotificationDto.getDeadline();
    }
}
