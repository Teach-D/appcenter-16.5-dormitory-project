package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.dto.request.notification.RequestPopupNotificationDto;
import com.example.appcenter_project.dto.response.notification.ResponsePopupNotificationDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.notification.PopupNotification;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.notification.PopupNotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.example.appcenter_project.exception.ErrorCode.IMAGE_NOT_FOUND;
import static com.example.appcenter_project.exception.ErrorCode.POPUP_NOTIFICATION_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PopupNotificationService {

    private final PopupNotificationRepository popupNotificationRepository;
    private final ImageRepository imageRepository;

    public void savePopupNotification(RequestPopupNotificationDto requestPopupNotificationDto, List<MultipartFile> images) {
        PopupNotification popupNotification = RequestPopupNotificationDto.dtoToEntity(requestPopupNotificationDto);
        popupNotificationRepository.save(popupNotification);

        saveImages(popupNotification, images);
    }

    public ResponsePopupNotificationDto findPopupNotification(Long popupNotificationId, HttpServletRequest request) {
        PopupNotification popupNotification = popupNotificationRepository.findById(popupNotificationId).orElseThrow(() -> new CustomException(POPUP_NOTIFICATION_NOT_FOUND));
        return ResponsePopupNotificationDto.entityToDto(popupNotification, request);
    }

    public List<ResponsePopupNotificationDto> findAllPopupNotifications(HttpServletRequest request) {
        return popupNotificationRepository.findAll().stream().map(popupNotification -> ResponsePopupNotificationDto.entityToDto(popupNotification, request)).toList();
    }

    public void updatePopupNotification(Long popupNotificationId, RequestPopupNotificationDto requestPopupNotificationDto, List<MultipartFile> images) {
        PopupNotification popupNotification = popupNotificationRepository.findById(popupNotificationId).orElseThrow(() -> new CustomException(POPUP_NOTIFICATION_NOT_FOUND));
        popupNotification.update(requestPopupNotificationDto);

        // 이미지가 제공된 경우에만 기존 이미지를 삭제하고 새로운 이미지를 저장
        if (images != null && !images.isEmpty()) {
            // 기존 이미지들이 있다면 파일 및 DB에서 삭제
            List<Image> existingImages = popupNotification.getImages();
            for (Image existingImage : existingImages) {
                File oldFile = new File(existingImage.getFilePath());
                if (oldFile.exists()) {
                    boolean deleted = oldFile.delete();
                    if (!deleted) {
                        log.warn("Failed to delete old PopupNotification image file: {}", existingImage.getFilePath());
                    }
                }
                // 기존 이미지 엔티티 삭제
                imageRepository.delete(existingImage);
            }
            popupNotification.getImages().clear();

            // 새로운 이미지들 저장
            saveImages(popupNotification, images);
        }
    }

    public void deletePopupNotification(Long popupNotificationId) {
        popupNotificationRepository.deleteById(popupNotificationId);
    }

    private void saveImages(PopupNotification popupNotification, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // User와 동일한 방식으로 경로 설정
            String basePath = System.getProperty("user.dir");
            String imagePath = basePath + "/images/popup-notification/";

            File directory = new File(imagePath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    log.error("Failed to create popup-notification directory: {}", imagePath);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }

            for (MultipartFile file : files) {
                // User 방식과 동일한 파일명 생성 패턴
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString();
                String imageFileName = "popup-notification_" + popupNotification.getId() + "_" + uuid + fileExtension;
                File destinationFile = new File(imagePath + imageFileName);

                try {
                    file.transferTo(destinationFile);
                    log.info("PopupNotification image saved successfully: {}", destinationFile.getAbsolutePath());

                    Image image = Image.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .isDefault(false)
                            .imageType(ImageType.POPUP_NOTIFICATION)
                            .boardId(popupNotification.getId())
                            .build();

                    imageRepository.save(image);
                    popupNotification.getImages().add(image);

                } catch (IOException e) {
                    log.error("Failed to save popup_notification image for popup_notification {}: ", popupNotification.getId(), e);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }
        }


    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".jpg"; // 기본 확장자
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".jpg"; // 확장자가 없으면 기본값
        }

        return fileName.substring(lastDotIndex).toLowerCase();
    }
}
