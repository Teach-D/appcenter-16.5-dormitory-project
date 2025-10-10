package com.example.appcenter_project.controller.notification;

import com.example.appcenter_project.dto.request.notification.RequestPopupNotificationDto;
import com.example.appcenter_project.dto.response.notification.ResponsePopupNotificationDto;
import com.example.appcenter_project.service.notification.PopupNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/popup-notifications")
public class PopupNotificationController implements PopupNotificationApiSpecification {

    private final PopupNotificationService popupNotificationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> savePopupNotification(@RequestPart RequestPopupNotificationDto requestPopupNotificationDto, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        popupNotificationService.savePopupNotification(requestPopupNotificationDto, images);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/{popupNotificationId}")
    public ResponseEntity<ResponsePopupNotificationDto> findPopupNotification(@PathVariable Long popupNotificationId, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(popupNotificationService.findPopupNotification(popupNotificationId, request));
    }

    @GetMapping
    public ResponseEntity<List<ResponsePopupNotificationDto>> findActivePopupNotifications(HttpServletRequest request) {
        return ResponseEntity.status(OK).body(popupNotificationService.findActivePopupNotifications(request));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ResponsePopupNotificationDto>> findAllPopupNotifications(HttpServletRequest request) {
        return ResponseEntity.status(OK).body(popupNotificationService.findAllPopupNotifications(request));
    }

    @PutMapping(value = "/{popupNotificationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePopupNotification(@PathVariable Long popupNotificationId, @RequestPart RequestPopupNotificationDto requestPopupNotificationDto, @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        popupNotificationService.updatePopupNotification(popupNotificationId, requestPopupNotificationDto, images);
        return ResponseEntity.status(ACCEPTED).build();
    }

    @DeleteMapping("/{popupNotificationId}")
    public ResponseEntity<Void> deletePopupNotification(@PathVariable Long popupNotificationId) {
        popupNotificationService.deletePopupNotification(popupNotificationId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
