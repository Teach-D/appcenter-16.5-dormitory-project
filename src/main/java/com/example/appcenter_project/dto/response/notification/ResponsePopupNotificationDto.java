package com.example.appcenter_project.dto.response.notification;

import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.PopupNotification;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.IMAGE_NOT_FOUND;

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
    private List<String> imagePath;

    public static ResponsePopupNotificationDto entityToDto(PopupNotification popupNotification, List<String> imageUrls) {
        return ResponsePopupNotificationDto.builder()
                .id(popupNotification.getId())
                .title(popupNotification.getTitle())
                .content(popupNotification.getContent())
                .notificationType(popupNotification.getNotificationType().toValue())
                .deadline(popupNotification.getDeadline())
                .createdDate(popupNotification.getCreatedDate().toLocalDate())
                .imagePath(imageUrls)
                .build();
    }
}
