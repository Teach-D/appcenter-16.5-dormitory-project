package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementNotificationService {

    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;
    private final NotificationRepository notificationRepository;

    private static final List<Role> DORMITORY_ROLES = List.of(
            Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER
    );

    public void sendDormitoryNotifications(Announcement announcement) {
        sendNotification(announcement, NotificationType.DORMITORY);
    }

    public void sendSupportersNotifications(Announcement announcement) {
        sendNotification(announcement, NotificationType.SUPPORTERS);
    }

    public void sendUnidormNotifications(Announcement announcement) {
        sendNotification(announcement, NotificationType.UNI_DORM);
    }

    private void sendNotification(Announcement announcement, NotificationType notificationType) {
        Notification notification = createNotification(announcement, notificationType);
        List<User> targetUser = findTargetUsers(notificationType);

        if (targetUser.isEmpty()) {
            return;
        }

        createUserNotifications(targetUser, notification);
        sendMessagesTo(targetUser, notification, notificationType);
    }

    public Notification createNotification(Announcement announcement, NotificationType notificationType) {
        String title = "새로운 공지사항이 올라왔어요!";

        Notification notification = Notification.of(
                title,
                announcement.getTitle(),
                notificationType,
                ApiType.ANNOUNCEMENT,
                announcement.getId()
        );

        return notificationRepository.save(notification);
    }

    private void createUserNotifications(List<User> targetUser, Notification notification) {
        List<UserNotification> userNotifications = targetUser.stream()
                .map(user -> UserNotification.of(user, notification))
                .toList();

        userNotificationRepository.saveAll(userNotifications);
    }

    private List<User> findTargetUsers(NotificationType notificationType) {
        return userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(notificationType, DORMITORY_ROLES);
    }

    private void sendMessagesTo(List<User> targetUser, Notification notification, NotificationType notificationType) {
        switch(notificationType) {
            case DORMITORY: sendDormitoryMessages(targetUser, notification.getTitle(), notification.getBody());
            case SUPPORTERS: sendSupporterMessages(targetUser, notification.getTitle(), notification.getBody());
            case UNI_DORM: sendUnidormMessages(targetUser, notification.getTitle(), notification.getBody());
        }
    }

    private void sendDormitoryMessages(List<User> targetUser, String title, String body) {
        for (User user : targetUser) {
            fcmMessageService.sendDormitoryNotification(user, title, body);
        }
    }

    private void sendSupporterMessages(List<User> targetUser, String title, String body) {
        for (User user : targetUser) {
            fcmMessageService.sendSupporterNotification(user, title, body);
        }
    }

    private void sendUnidormMessages(List<User> targetUser, String title, String body) {
        for (User user : targetUser) {
            fcmMessageService.sendUnidormNotification(user, title, body);
        }
    }

}