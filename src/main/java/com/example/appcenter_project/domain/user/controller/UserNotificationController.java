package com.example.appcenter_project.domain.user.controller;

import com.example.appcenter_project.domain.user.dto.response.ResponseUserNotificationDto;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.user.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-notifications")
public class UserNotificationController implements UserNotificationApiSpecification {

    private final UserNotificationService userNotificationService;

    @PostMapping("/preferences")
    public ResponseEntity<Void> addReceiveNotificationType(@AuthenticationPrincipal CustomUserDetails user, @RequestParam List<String> notificationTypes) {
        userNotificationService.addReceiveNotificationType(user.getId(), notificationTypes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/group-order/keyword")
    public ResponseEntity<Void> addGroupOrderKeyword(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String keyword) {
        userNotificationService.addGroupOrderKeyword(user.getId(), keyword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/group-order/category")
    public ResponseEntity<Void> addGroupOrderCategory(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String category) {
        userNotificationService.addGroupOrderCategory(user.getId(), GroupOrderType.from(category));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<ResponseUserNotificationDto> findReceiveNotificationType(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userNotificationService.findReceiveNotificationType(user.getId()));
    }


    @GetMapping("/group-order/keyword")
    public ResponseEntity<List<String>> findUserGroupOrderKeyword(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userNotificationService.findUserGroupOrderKeyword(user.getId()));
    }

    @GetMapping("/group-order/category")
    public ResponseEntity<List<String>> findUserGroupOrderCategory(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(OK).body(userNotificationService.findUserGroupOrderCategory(user.getId()));
    }

    @PutMapping("/group-order/keyword")
    public ResponseEntity<Void> updateGroupOrderKeyword(@AuthenticationPrincipal CustomUserDetails user,
                                                        @RequestParam("beforeKeyword") String beforeKeyword, @RequestParam("afterKeyword") String afterKeyword) {
        userNotificationService.updateGroupOrderKeyword(user.getId(), beforeKeyword, afterKeyword);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/group-order/category")
    public ResponseEntity<Void> updateGroupOrderCategory(@AuthenticationPrincipal CustomUserDetails user,
                                                        @RequestParam("beforeKeyword") String beforeCategory, @RequestParam("afterKeyword") String afterCategory) {
        userNotificationService.updateGroupOrderCategory(user.getId(), GroupOrderType.from(beforeCategory), GroupOrderType.from(afterCategory));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/preferences")
    public ResponseEntity<Void> deleteReceiveNotificationType(@AuthenticationPrincipal CustomUserDetails user, @RequestParam List<String> notificationTypes) {
        userNotificationService.deleteReceiveNotificationType(user.getId(), notificationTypes);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/group-order/keyword")
    public ResponseEntity<Void> deleteUserGroupOrderKeyword(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String keyword) {
        userNotificationService.deleteUserGroupOrderKeyword(user.getId(), keyword);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/group-order/category")
    public ResponseEntity<Void> deleteUserGroupOrderCategory(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String category) {
        userNotificationService.deleteUserGroupOrderCategory(user.getId(), GroupOrderType.from(category));
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/notification/{notificationId}")
    public ResponseEntity<Void> deleteUserNotification(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long notificationId) {
        userNotificationService.deleteUserNotification(user.getId(), notificationId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
