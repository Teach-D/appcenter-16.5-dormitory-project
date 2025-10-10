package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.notification.RequestPopupNotificationDto;
import com.example.appcenter_project.dto.response.notification.ResponsePopupNotificationDto;
import com.example.appcenter_project.entity.notification.PopupNotification;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.notification.PopupNotificationRepository;
import com.example.appcenter_project.service.image.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.POPUP_NOTIFICATION_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PopupNotificationService {

    private final PopupNotificationRepository popupNotificationRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    public void savePopupNotification(RequestPopupNotificationDto requestPopupNotificationDto, List<MultipartFile> images) {
        PopupNotification popupNotification = RequestPopupNotificationDto.dtoToEntity(requestPopupNotificationDto);
        popupNotificationRepository.save(popupNotification);

        imageService.saveImages(ImageType.POPUP_NOTIFICATION, popupNotification.getId(), images);
    }

    public ResponsePopupNotificationDto findPopupNotification(Long popupNotificationId, HttpServletRequest request) {
        PopupNotification popupNotification = popupNotificationRepository.findById(popupNotificationId).orElseThrow(() -> new CustomException(POPUP_NOTIFICATION_NOT_FOUND));
        List<String> imageUrls = imageService.findImages(ImageType.POPUP_NOTIFICATION, popupNotificationId, request).stream()
                .map(ImageLinkDto::getImageUrl)
                .toList();

        return ResponsePopupNotificationDto.entityToDto(popupNotification, imageUrls);
    }

    public List<ResponsePopupNotificationDto> findActivePopupNotifications(HttpServletRequest request) {
        LocalDate now = LocalDate.now();

        return popupNotificationRepository.findAll().stream()
                .filter(popupNotification -> {
                    LocalDate startDate = popupNotification.getStartDate();
                    LocalDate deadline = popupNotification.getDeadline();
                    
                    if (startDate != null && deadline != null) {
                        // 현재 날짜가 startDate 이상이고 deadline 이하인 경우만 포함
                        return !now.isBefore(startDate) && !now.isAfter(deadline);
                    }
                    
                    return false;
                })
                .map(popupNotification ->
                        ResponsePopupNotificationDto.entityToDto(
                                popupNotification, 
                                imageService.findStaticImageUrls(ImageType.POPUP_NOTIFICATION, popupNotification.getId(), request)
                        )
                )
                .toList();
    }

    public void updatePopupNotification(Long popupNotificationId, RequestPopupNotificationDto requestPopupNotificationDto, List<MultipartFile> images) {
        PopupNotification popupNotification = popupNotificationRepository.findById(popupNotificationId).orElseThrow(() -> new CustomException(POPUP_NOTIFICATION_NOT_FOUND));
        popupNotification.update(requestPopupNotificationDto);

        imageService.updateImages(ImageType.POPUP_NOTIFICATION, popupNotificationId, images);
    }

    public void deletePopupNotification(Long popupNotificationId) {
        popupNotificationRepository.deleteById(popupNotificationId);
        imageService.deleteImages(ImageType.POPUP_NOTIFICATION, popupNotificationId);
    }

    public List<ResponsePopupNotificationDto> findAllPopupNotifications(HttpServletRequest request) {
        return popupNotificationRepository.findAll().stream().map(popupNotification ->
                ResponsePopupNotificationDto.entityToDto(popupNotification, imageService.findStaticImageUrls(ImageType.POPUP_NOTIFICATION, popupNotification.getId(), request))).toList();

    }
}
