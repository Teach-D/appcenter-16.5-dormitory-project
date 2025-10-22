package com.example.appcenter_project.domain.user.service;

import com.example.appcenter_project.domain.user.dto.response.ResponseUserNotificationDto;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNotificationService {

    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;

    public void addGroupOrderKeyword(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (user.getGroupOrderKeywords().contains(keyword)) {
            throw new CustomException(USER_GROUP_ORDER_KEYWORD_ALREADY_EXISTS);
        }

        user.getGroupOrderKeywords().add(keyword);
    }

    public void addGroupOrderCategory(Long userId, GroupOrderType groupOrderType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (user.getGroupOrderTypes().contains(groupOrderType)) {
            throw new CustomException(USER_GROUP_ORDER_CATEGORY_ALREADY_EXISTS);
        }

        user.getGroupOrderTypes().add(groupOrderType);
    }

    public List<String> findUserGroupOrderKeyword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return user.getGroupOrderKeywords();
    }

    public List<String> findUserGroupOrderCategory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return user.getGroupOrderTypes().stream().map(GroupOrderType::toValue).toList();
    }

    public void updateGroupOrderKeyword(Long userId, String beforeKeyword, String afterKeyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (user.getGroupOrderKeywords().contains(afterKeyword)) {
            throw new CustomException(USER_GROUP_ORDER_KEYWORD_ALREADY_EXISTS);
        }

        int index = user.getGroupOrderKeywords().indexOf(beforeKeyword);
        user.getGroupOrderKeywords().set(index, afterKeyword);

    }

    public void updateGroupOrderCategory(Long userId, GroupOrderType beforeCategory, GroupOrderType afterCategory) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (user.getGroupOrderTypes().contains(afterCategory)) {
            throw new CustomException(USER_GROUP_ORDER_CATEGORY_ALREADY_EXISTS);
        }

        int index = user.getGroupOrderTypes().indexOf(beforeCategory);
        user.getGroupOrderTypes().set(index, afterCategory);
    }

    public void deleteUserGroupOrderKeyword(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        user.getGroupOrderKeywords().remove(keyword);
    }

    public void deleteUserGroupOrderCategory(Long userId, GroupOrderType category) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        user.getGroupOrderTypes().remove(category);
    }

    public void addReceiveNotificationType(Long userId, List<String> notificationTypes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        for (String notificationType : notificationTypes) {
            if (!user.getReceiveNotificationTypes().contains(NotificationType.from(notificationType))) {
                user.addReceiveNotificationType(NotificationType.from(notificationType));
            }
        }
    }

    public void deleteReceiveNotificationType(Long userId, List<String> notificationTypes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        for (String notificationType : notificationTypes) {
            if (user.getReceiveNotificationTypes().contains(NotificationType.from(notificationType))) {
                user.deleteReceiveNotificationType(NotificationType.from(notificationType));
            }
        }
    }

    public void deleteUserNotification(Long userId, Long notificationId) {
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(userId, notificationId).orElseThrow(() -> new CustomException(USER_NOTIFICATION_NOT_FOUND));
        userNotificationRepository.delete(userNotification);
    }

    public ResponseUserNotificationDto findReceiveNotificationType(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<NotificationType> receiveTypes = user.getReceiveNotificationTypes();

        ResponseUserNotificationDto responseDto = ResponseUserNotificationDto.builder()
                .roommateNotification(receiveTypes.contains(NotificationType.ROOMMATE))
                .groupOrderNotification(receiveTypes.contains(NotificationType.GROUP_ORDER))
                .dormitoryNotification(receiveTypes.contains(NotificationType.DORMITORY))
                .unidormNotification(receiveTypes.contains(NotificationType.UNI_DORM))
                .supportersNotification(receiveTypes.contains(NotificationType.SUPPORTERS))
                .build();

        return responseDto;
    }
}
