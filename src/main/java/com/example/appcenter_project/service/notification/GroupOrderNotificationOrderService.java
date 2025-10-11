package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.ApiType;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

        List<User> allUsers = userRepository.findAll();

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
            String title = "[" + userStringEntry.getValue() + "]" + " 공동구매 게시글이 등록되었습니다!";

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
        List<User> allUsers = userRepository.findAll();

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
            String title = "[" + userTypeEntry.getValue().toValue() + "]" + " 공동구매 게시글이 등록되었습니다!";

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