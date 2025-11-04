package com.example.appcenter_project.domain.notification.entity;

import com.example.appcenter_project.domain.notification.dto.request.RequestPopupNotificationDto;
import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    public void update(RequestPopupNotificationDto requestDto) {
        this.title = requestDto.getTitle();
        this.content = requestDto.getContent();
        this.notificationType = NotificationType.from(requestDto.getNotificationType());
        this.startDate = requestDto.getStartDate();
        this.deadline = requestDto.getDeadline();
    }

    public static PopupNotification from(RequestPopupNotificationDto requestPopupNotificationDto) {
        return PopupNotification.builder()
                .title(requestPopupNotificationDto.getTitle())
                .content(requestPopupNotificationDto.getContent())
                .notificationType(NotificationType.from(requestPopupNotificationDto.getNotificationType()))
                .startDate(requestPopupNotificationDto.getStartDate())
                .deadline(requestPopupNotificationDto.getDeadline())
                .build();
    }
}