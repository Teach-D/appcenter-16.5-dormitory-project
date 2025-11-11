package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.domain.notification.dto.request.RequestPopupNotificationDto;
import com.example.appcenter_project.domain.notification.dto.response.ResponsePopupNotificationDto;
import com.example.appcenter_project.domain.notification.entity.PopupNotification;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.notification.repository.PopupNotificationRepository;
import com.example.appcenter_project.common.image.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.POPUP_NOTIFICATION_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PopupNotificationService {

    private final PopupNotificationRepository popupNotificationRepository;
    private final ImageService imageService;

    public void savePopupNotification(RequestPopupNotificationDto requestDto, List<MultipartFile> images) {
        PopupNotification popupNotification = PopupNotification.from(requestDto);
        popupNotificationRepository.save(popupNotification);

        imageService.saveImages(ImageType.POPUP_NOTIFICATION, popupNotification.getId(), images);
    }

    public ResponsePopupNotificationDto findPopupNotification(Long popupNotificationId, HttpServletRequest request) {
        PopupNotification popupNotification = popupNotificationRepository.findById(popupNotificationId).orElseThrow(() -> new CustomException(POPUP_NOTIFICATION_NOT_FOUND));
        List<String> imageUrls = imageService.findImages(ImageType.POPUP_NOTIFICATION, popupNotificationId, request).stream()
                .map(ImageLinkDto::getImageUrl)
                .toList();

        return ResponsePopupNotificationDto.of(popupNotification, imageUrls);
    }

    public List<ResponsePopupNotificationDto> findActivePopupNotifications(HttpServletRequest request) {
        LocalDate now = LocalDate.now();

        return popupNotificationRepository.findActivePopupNotifications(now).stream()
                .map(popupNotification ->
                        ResponsePopupNotificationDto.of(
                                popupNotification,
                                findImageUrl(request, popupNotification)
                        )
                )
                .toList();
    }

    private List<String> findImageUrl(HttpServletRequest request, PopupNotification popupNotification) {
        return imageService.findStaticImageUrls(ImageType.POPUP_NOTIFICATION, popupNotification.getId(), request);
    }

    public List<ResponsePopupNotificationDto> findAllPopupNotifications(HttpServletRequest request) {
        return popupNotificationRepository.findAll().stream().map(popupNotification ->
                ResponsePopupNotificationDto.of(popupNotification, findImageUrl(request, popupNotification))).toList();

    }

    public void updatePopupNotification(Long popupNotificationId, RequestPopupNotificationDto requestDto, List<MultipartFile> images) {
        PopupNotification popupNotification = popupNotificationRepository.findById(popupNotificationId).orElseThrow(() -> new CustomException(POPUP_NOTIFICATION_NOT_FOUND));
        popupNotification.update(requestDto);

        if (images == null || images.isEmpty()) {
            return;
        }

        imageService.updateImages(ImageType.POPUP_NOTIFICATION, popupNotificationId, images);
    }

    public void deletePopupNotification(Long popupNotificationId) {
        popupNotificationRepository.deleteById(popupNotificationId);
        imageService.deleteImages(ImageType.POPUP_NOTIFICATION, popupNotificationId);
    }
}