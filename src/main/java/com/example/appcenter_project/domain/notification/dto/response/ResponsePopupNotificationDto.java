package com.example.appcenter_project.domain.notification.dto.response;

import com.example.appcenter_project.domain.notification.entity.PopupNotification;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Builder
@Getter
public class ResponsePopupNotificationDto {

    private Long id;
    private String title;
    private String content;
    private String notificationType;
    private LocalDate deadline;
    private LocalDate createdDate;
    private LocalDate startDate;
    private List<String> imagePath;

    public static ResponsePopupNotificationDto of(PopupNotification popupNotification, List<String> imageUrls) {
        return ResponsePopupNotificationDto.builder()
                .id(popupNotification.getId())
                .title(popupNotification.getTitle())
                .content(popupNotification.getContent())
                .notificationType(popupNotification.getNotificationType().toValue())
                .startDate(popupNotification.getStartDate())
                .deadline(popupNotification.getDeadline())
                .createdDate(popupNotification.getCreatedDate().toLocalDate())
                .imagePath(imageUrls)
                .build();
    }
}
