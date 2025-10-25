package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderComment;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.shared.enums.ApiType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;
    private final UserRepository userRepository;

    public void sendNotifications(GroupOrder groupOrder) {
        // 키워드로 매칭된 유저들
        Set<User> keywordSendUsers = sendKeywordNotifications(groupOrder);

        // 카테고리로 매칭 (키워드로 이미 알림받은 유저 제외)
        sendCategoryNotificationsWithoutKeyword(groupOrder, keywordSendUsers);
    }

    private Set<User> sendKeywordNotifications(GroupOrder groupOrder) {
        String combinedText = groupOrder.getTitle() + groupOrder.getDescription();

        List<Role> dormitoryRoles = Arrays.asList(Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER);

        List<User> sendUsers = userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(NotificationType.GROUP_ORDER, dormitoryRoles);

        Map<User, String> userKeywordMap = sendUsers.stream()
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

    private void sendCategoryNotificationsWithoutKeyword(GroupOrder groupOrder, Set<User> excludedUsers) {
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

    public void sendOpenChatLinkNotification(GroupOrder groupOrder) {
        Set<Long> likedUserIds = sendNotificationToLikedUser(groupOrder);
        sendNotificationToCommentUserWithoutLikedUser(groupOrder, likedUserIds);
    }

    private Set<Long> sendNotificationToLikedUser(GroupOrder groupOrder) {
        List<User> likedUsers = extractLikedUsers(groupOrder);

        if (likedUsers.isEmpty()) {
            return Set.of();
        }

        sendNotifications(likedUsers, "좋아요한 공동구매 게시글의 오픈채팅방이 만들어졌어요!", groupOrder);

        return extractUserIds(likedUsers);
    }

    private static Set<Long> extractUserIds(List<User> likedUsers) {
        return likedUsers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
    }

    private void sendNotifications(List<User> likedUsers, String title, GroupOrder groupOrder) {
        Notification notification = createGroupOrderNotification(title, groupOrder);
        notificationRepository.save(notification);

        createUserNotifications(likedUsers, notification);
        sendFcmMessage(likedUsers, notification);
    }

    private void sendFcmMessage(List<User> likedUsers, Notification notification) {
        likedUsers.forEach(user ->
                fcmMessageService.sendGroupOrderNotification(user, notification.getTitle(), notification.getBody()));
    }

    private void createUserNotifications(List<User> likedUsers, Notification notification) {
        List<UserNotification> userNotifications = likedUsers.stream()
                .map(user -> UserNotification.of(user, notification))
                .toList();

        userNotificationRepository.saveAll(userNotifications);
    }

    private static List<User> extractLikedUsers(GroupOrder groupOrder) {
        return groupOrder.getGroupOrderLikeList().stream()
                .map(GroupOrderLike::getUser)
                .toList();
    }

    private void sendNotificationToCommentUserWithoutLikedUser(GroupOrder groupOrder, Set<Long> likedUserIds) {
        List<User> commentUsers = findCommentUsersWithOutLike(groupOrder, likedUserIds);

        if (commentUsers.isEmpty()) {
            return;
        }

        sendNotifications(commentUsers, "댓글을 단 공동구매 게시글의 오픈채팅방이 만들어졌어요!", groupOrder);

    }

    private static List<User> findCommentUsersWithOutLike(GroupOrder groupOrder, Set<Long> likedUserIds) {
        return groupOrder.getGroupOrderCommentList().stream()
                .map(GroupOrderComment::getUser)
                .filter(user -> !likedUserIds.contains(user.getId()))
                .distinct()
                .toList();
    }

    private Notification createGroupOrderNotification(String title, GroupOrder groupOrder) {
        return Notification.of(
                title,
                groupOrder.getTitle(),
                NotificationType.GROUP_ORDER,
                ApiType.GROUP_ORDER,
                groupOrder.getId()
        );
    }

}
