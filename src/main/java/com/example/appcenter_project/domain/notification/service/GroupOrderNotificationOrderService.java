package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderNotificationOrderService {

    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;
    private final NotificationRepository notificationRepository;

    public void saveAndSendGroupOrderNotification(GroupOrder groupOrder) {
        // 키워드로 매칭된 유저들
        Set<User> notifiedUsers = saveAndSendUserGroupOrderByKeyword(groupOrder);

        // 카테고리로 매칭 (키워드로 이미 알림받은 유저 제외)
        saveAndSendUserGroupOrderByCategory(groupOrder, notifiedUsers);
    }

    private Set<User> saveAndSendUserGroupOrderByKeyword(GroupOrder groupOrder) {
        String combinedText = groupOrder.getTitle() + groupOrder.getDescription();

        List<Role> dormitoryUserRoles = Arrays.asList(Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER);

        List<User> allUsers = userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(NotificationType.GROUP_ORDER, dormitoryUserRoles);

        // 각 유저당 매칭된 첫 번째 키워드만
        Map<User, String> userKeywordMap = allUsers.stream()
                .map(user -> {
                    Optional<String> matchedKeyword = user.getGroupOrderKeywords().stream()
                            .filter(keyword -> combinedText.contains(keyword))
                            .findFirst();
                    return matchedKeyword.map(keyword -> Map.entry(user, keyword));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<User, String> userStringEntry : userKeywordMap.entrySet()) {
            String title = "[" + userStringEntry.getValue() + "]" + " 공동구매 새 글이 올라왔어요";

            Notification notification = null;

            if (notificationRepository.existsByBoardIdAndTitle(groupOrder.getId(), title)) {
                notification = notificationRepository.findByBoardIdAndTitle(groupOrder.getId(), title);
            } else {
                notification = Notification.builder()
                        .boardId(groupOrder.getId())
                        .title(title)
                        .body(groupOrder.getTitle())
                        .notificationType(NotificationType.GROUP_ORDER)
                        .apiType(ApiType.GROUP_ORDER)
                        .build();

                notificationRepository.save(notification);
            }


            UserNotification userNotification = UserNotification.of(userStringEntry.getKey(), notification);

            userNotificationRepository.save(userNotification);

            fcmMessageService.sendGroupOrderNotification(
                    userStringEntry.getKey(),
                    title,
                    groupOrder.getTitle()
            );
        }

        // 키워드로 알림받은 유저들 반환
        return userKeywordMap.keySet();
    }

    private void saveAndSendUserGroupOrderByCategory(GroupOrder groupOrder, Set<User> excludedUsers) {
        List<Role> dormitoryUserRoles = Arrays.asList(Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER);

        List<User> allUsers = userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(NotificationType.GROUP_ORDER, dormitoryUserRoles);

        // 각 유저당 매칭된 첫 번째 GroupOrderType만 (이미 알림받은 유저 제외)
        Map<User, GroupOrderType> userTypeMap = allUsers.stream()
                .filter(user -> !excludedUsers.contains(user)) // 키워드로 이미 알림받은 유저 제외
                .map(user -> {
                    Optional<GroupOrderType> matchedType = user.getGroupOrderTypes().stream()
                            .filter(type -> type.equals(groupOrder.getGroupOrderType()))
                            .findFirst();
                    return matchedType.map(type -> Map.entry(user, type));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<User, GroupOrderType> userTypeEntry : userTypeMap.entrySet()) {
            String title = "[" + userTypeEntry.getValue().toValue() + "]" + " 공동구매 새 글이 올라왔어요";

            Notification notification = null;

            if (notificationRepository.existsByBoardIdAndTitle(groupOrder.getId(), title)) {
                notification = notificationRepository.findByBoardIdAndTitle(groupOrder.getId(), title);
            } else {
                notification = Notification.builder()
                        .boardId(groupOrder.getId())
                        .title(title)
                        .body(groupOrder.getTitle())
                        .notificationType(NotificationType.GROUP_ORDER)
                        .apiType(ApiType.GROUP_ORDER)
                        .build();

                notificationRepository.save(notification);
            }

            UserNotification userNotification = UserNotification.of(userTypeEntry.getKey(), notification);
            userNotificationRepository.save(userNotification);

            fcmMessageService.sendGroupOrderNotification(
                    userTypeEntry.getKey(),
                    title,
                    groupOrder.getTitle()
            );
        }
    }
}