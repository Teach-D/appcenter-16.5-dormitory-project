package com.example.appcenter_project.domain.notification.controller;

import com.example.appcenter_project.domain.notification.dto.request.RequestNotificationDto;
import com.example.appcenter_project.domain.notification.dto.response.ResponseNotificationDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController implements NotificationApiSpecification {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> saveNotification(@RequestBody RequestNotificationDto requestNotificationDto) {
        notificationService.saveNotification(requestNotificationDto);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ResponseNotificationDto>> findNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(notificationService.findNotifications(user.getId()));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ResponseNotificationDto> findNotification(@AuthenticationPrincipal CustomUserDetails user, @PathVariable("notificationId") Long notificationId) {
        return ResponseEntity.status(OK).body(notificationService.findNotification(user.getId(), notificationId));
    }

    @PostMapping("/student-number/{studentNumber}")
    public ResponseEntity<Void> saveNotificationByStudentNumber(@RequestBody RequestNotificationDto requestNotificationDto, String studentNumber) {
        notificationService.saveNotificationByStudentNumber(requestNotificationDto, studentNumber);
        return ResponseEntity.status(CREATED).build();
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<Void> updateNotification(@PathVariable Long notificationId, @RequestBody RequestNotificationDto requestNotificationDto) {
        notificationService.updateNotification(notificationId, requestNotificationDto);
        return ResponseEntity.status(OK).build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
