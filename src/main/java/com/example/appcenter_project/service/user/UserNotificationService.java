package com.example.appcenter_project.service.user;

import com.example.appcenter_project.dto.response.user.ResponseUserAgreementDto;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.entity.user.UserGroupOrderCategory;
import com.example.appcenter_project.entity.user.UserGroupOrderKeyword;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserGroupOrderCategoryRepository;
import com.example.appcenter_project.repository.user.UserGroupOrderKeywordRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserNotificationService {

    private final UserGroupOrderKeywordRepository userGroupOrderKeywordRepository;
    private final UserGroupOrderCategoryRepository userGroupOrderCategoryRepository;
    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;

    public void addGroupOrderKeyword(Long userId, String keyword) {
        if (userGroupOrderKeywordRepository.existsByKeyword(keyword)) {
            throw new CustomException(USER_KEYWORD_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        UserGroupOrderKeyword build = UserGroupOrderKeyword.builder()
                .user(user)
                .keyword(keyword)
                .build();

        userGroupOrderKeywordRepository.save(build);
    }

    public List<String> findUserGroupOrderKeyword(Long userId) {
        return userGroupOrderKeywordRepository.findByUserId(userId).stream().map(u -> u.getKeyword()).toList();
    }

    public void updateGroupOrderKeyword(Long userId, String beforeKeyword, String afterKeyword) {
        if (userGroupOrderKeywordRepository.existsByKeyword(afterKeyword)) {
            throw new CustomException(USER_KEYWORD_ALREADY_EXISTS);
        }
        UserGroupOrderKeyword userGroupOrderKeyword = userGroupOrderKeywordRepository
                .findByUserIdAndKeyword(userId, beforeKeyword).orElseThrow(() -> new CustomException(USER_GROUP_ORDER_KEYWORD_NOT_FOUND));
        userGroupOrderKeyword.updateKeyword(afterKeyword);
    }

    public void deleteUserGroupOrderKeyword(Long userId, String keyword) {
        userGroupOrderKeywordRepository.deleteByUserIdAndKeyword(userId, keyword);
    }

    public void addGroupOrderCategory(Long userId, GroupOrderType groupOrderType) {
        if (userGroupOrderCategoryRepository.existsByCategory(groupOrderType)) {
            throw new CustomException(USER_KEYWORD_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        UserGroupOrderCategory userGroupOrderCategory = UserGroupOrderCategory.builder()
                .user(user)
                .category(groupOrderType)
                .build();

        userGroupOrderCategoryRepository.save(userGroupOrderCategory);
    }


    public List<String> findUserGroupOrderCategory(Long userId) {
        return userGroupOrderCategoryRepository.findByUserId(userId)
                .stream()
                .map(UserGroupOrderCategory::getCategory)
                .map(GroupOrderType::toValue)
                .collect(Collectors.toList());
    }

    public void updateGroupOrderCategory(Long userId, GroupOrderType beforeCategory, GroupOrderType afterCategory) {
        if (userGroupOrderCategoryRepository.existsByCategory(afterCategory)) {
            throw new CustomException(USER_KEYWORD_ALREADY_EXISTS);
        }

        if (!userGroupOrderCategoryRepository.existsByCategory(beforeCategory)) {
            throw new CustomException(USER_KEYWORD_NOT_FOUND);
        }

        UserGroupOrderCategory userGroupOrderCategory = userGroupOrderCategoryRepository.findByUserIdAndCategory(userId, beforeCategory).orElseThrow(() -> new CustomException(USER_KEYWORD_NOT_FOUND));
        userGroupOrderCategory.updateKeyword(afterCategory);
    }

    public void deleteUserGroupOrderCategory(Long userId, GroupOrderType category) {
        UserGroupOrderCategory userGroupOrderCategory = userGroupOrderCategoryRepository.findByUserIdAndCategory(userId, category).orElseThrow(() -> new CustomException(USER_KEYWORD_NOT_FOUND));
        userGroupOrderCategoryRepository.delete(userGroupOrderCategory);
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
