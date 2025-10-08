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

    public static ResponsePopupNotificationDto entityToDto(PopupNotification popupNotification, HttpServletRequest request) {
        List<String> imagePath = new ArrayList<>();

        for (Image image : popupNotification.getImages()) {
            imagePath.add(getPopupNotificationImage(image, request));
        }

        return ResponsePopupNotificationDto.builder()
                .id(popupNotification.getId())
                .title(popupNotification.getTitle())
                .content(popupNotification.getContent())
                .notificationType(popupNotification.getNotificationType().toValue())
                .deadline(popupNotification.getDeadline())
                .createdDate(popupNotification.getCreatedDate().toLocalDate())
                .imagePath(imagePath)
                .build();
    }

    public static String getPopupNotificationImage(Image image, HttpServletRequest request) {
        File file = new File(image.getFilePath());
        log.info("Checking popup-notification image file: {}", image.getFilePath());
        log.info("File exists: {}", file.exists());

        if (!file.exists()) {
            log.error("popup-notification image file not found: {}", image.getFilePath());
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        // 이미지 URL 생성
        String baseUrl = getBaseUrl(request);
        String imageUrl = baseUrl + "/api/images/popup-notification/" + image.getId();

        // 정적 리소스 URL 생성
        String staticImageUrl = getStaticGroupOrderImageUrl(image.getFilePath(), baseUrl);
        String changeUrl = staticImageUrl.replace("http", "https");

        return changeUrl;
    }

    // 유틸리티: 베이스 URL 생성 (ImageService와 동일)
    private static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // 기본 포트가 아닌 경우에만 포트 추가
        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }

        baseUrl.append(contextPath);
        return baseUrl.toString();
    }

    private static String getStaticGroupOrderImageUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/images/popup-notification/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for popup-notification image path: {}", filePath);
            return null;
        }
    }
}
