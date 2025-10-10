package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.response.user.ResponseUserAgreementDto;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

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

    public ResponseUserAgreementDto findReceiveNotificationType(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        boolean privacyAgreed = user.isPrivacyAgreed();
        boolean termsAgreed = user.isTermsAgreed();

        return ResponseUserAgreementDto.of(termsAgreed, privacyAgreed);
    }
}
