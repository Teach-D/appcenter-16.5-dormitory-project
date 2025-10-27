package com.example.appcenter_project.domain.user.service;

import com.example.appcenter_project.domain.user.dto.response.ResponseUserNotificationDto;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
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

    // ========== Public Methods ========== //

    public void addReceiveNotificationType(Long userId, List<String> notificationTypes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        notificationTypes.forEach(notificationType -> {
            if (user.isNotHaveNotificationType(notificationType)) {
                user.addReceiveNotificationType(NotificationType.from(notificationType));
            }
        });
    }

    public void addGroupOrderKeyword(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (isAlreadyHaveKeyword(keyword, user)) {
            throw new CustomException(USER_GROUP_ORDER_KEYWORD_ALREADY_EXISTS);
        }

        addKeyword(keyword, user);
    }

    public void addGroupOrderCategory(Long userId, GroupOrderType groupOrderType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (isAlreadyHaveType(groupOrderType, user)) {
            throw new CustomException(USER_GROUP_ORDER_CATEGORY_ALREADY_EXISTS);
        }
        addType(groupOrderType, user);
    }

    public ResponseUserNotificationDto findReceiveNotificationType(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<NotificationType> receiveTypes = user.getReceiveNotificationTypes();

        return ResponseUserNotificationDto.builder()
                .roommateNotification(receiveTypes.contains(NotificationType.ROOMMATE))
                .groupOrderNotification(receiveTypes.contains(NotificationType.GROUP_ORDER))
                .dormitoryNotification(receiveTypes.contains(NotificationType.DORMITORY))
                .unidormNotification(receiveTypes.contains(NotificationType.UNI_DORM))
                .supportersNotification(receiveTypes.contains(NotificationType.SUPPORTERS))
                .build();
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

    public void updateGroupOrderCategory(Long userId, GroupOrderType beforeCategory, GroupOrderType afterCategory) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (isAlreadyHaveType(afterCategory, user)) {
            throw new CustomException(USER_GROUP_ORDER_CATEGORY_ALREADY_EXISTS);
        }
        changeType(beforeCategory, afterCategory, user);
    }

    public void updateGroupOrderKeyword(Long userId, String beforeKeyword, String afterKeyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (isAlreadyHaveKeyword(afterKeyword, user)) {
            throw new CustomException(USER_GROUP_ORDER_KEYWORD_ALREADY_EXISTS);
        }
        changeKeyword(beforeKeyword, afterKeyword, user);

    }

    public void deleteReceiveNotificationType(Long userId, List<String> notificationTypes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        notificationTypes.forEach(notificationType -> {
            if (user.isHaveNotificationType(notificationType)) {
                user.deleteReceiveNotificationType(NotificationType.from(notificationType));
            }
        });
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

    public void deleteUserNotification(Long userId, Long notificationId) {
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(userId, notificationId).orElseThrow(() -> new CustomException(USER_NOTIFICATION_NOT_FOUND));
        userNotificationRepository.delete(userNotification);
    }

    // ========== Private Methods ========== //

    private static boolean isAlreadyHaveKeyword(String keyword, User user) {
        return user.getGroupOrderKeywords().contains(keyword);
    }

    private static void addKeyword(String keyword, User user) {
        user.getGroupOrderKeywords().add(keyword);
    }

    private static boolean isAlreadyHaveType(GroupOrderType groupOrderType, User user) {
        return user.getGroupOrderTypes().contains(groupOrderType);
    }

    private static void addType(GroupOrderType groupOrderType, User user) {
        user.getGroupOrderTypes().add(groupOrderType);
    }

    private static void changeType(GroupOrderType beforeCategory, GroupOrderType afterCategory, User user) {
        int index = user.getGroupOrderTypes().indexOf(beforeCategory);

        if (index != -1) {
            throw new CustomException(USER_GROUP_ORDER_TYPE_NOT_FOUND);
        }

        user.getGroupOrderTypes().set(index, afterCategory);
    }

    private static void changeKeyword(String beforeKeyword, String afterKeyword, User user) {
        int index = user.getGroupOrderKeywords().indexOf(beforeKeyword);

        if (index != -1) {
            throw new CustomException(USER_GROUP_ORDER_KEYWORD_NOT_FOUND);
        }

        user.getGroupOrderKeywords().set(index, afterKeyword);
    }
}
