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

import java.util.Arrays;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementNotificationService {

    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;
    private final NotificationRepository notificationRepository;

    public void saveAndSendDormitoryNotification(Announcement announcement) {
        String title = "새로운 공지사항이 올라왔어요!";

        Notification notification = Notification.builder()
                .boardId(announcement.getId())
                .title(title)
                .body(announcement.getTitle())
                .notificationType(NotificationType.DORMITORY)
                .apiType(ApiType.ANNOUNCEMENT)
                .build();

        notificationRepository.save(notification);

        List<Role> dormitoryUserRoles = Arrays.asList(Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER);

        for (User user : userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(NotificationType.DORMITORY, dormitoryUserRoles)) {
            UserNotification userNotification = UserNotification.of(user, notification);
            userNotificationRepository.save(userNotification);
            fcmMessageService.sendDormitoryNotification(user, title, announcement.getTitle());
        }
    }

    public void saveAndSendSupportersNotification(Announcement announcement) {
        String title = "새로운 공지사항이 올라왔어요!";

        Notification notification = Notification.builder()
                .boardId(announcement.getId())
                .title(title)
                .body(announcement.getTitle())
                .notificationType(NotificationType.SUPPORTERS)
                .apiType(ApiType.ANNOUNCEMENT)
                .build();

        notificationRepository.save(notification);

        List<Role> dormitoryUserRoles = Arrays.asList(Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER);

        for (User user : userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(NotificationType.SUPPORTERS, dormitoryUserRoles)) {
            UserNotification userNotification = UserNotification.of(user, notification);
            userNotificationRepository.save(userNotification);
            fcmMessageService.sendSupportersNotification(user, title, announcement.getTitle());
        }
    }

    public void saveAndSendUnidormNotification(Announcement announcement) {
        String title = "새로운 공지사항이 올라왔어요!";

        Notification notification = Notification.builder()
                .boardId(announcement.getId())
                .title(title)
                .body(announcement.getTitle())
                .notificationType(NotificationType.UNI_DORM)
                .apiType(ApiType.ANNOUNCEMENT)
                .build();

        notificationRepository.save(notification);

        List<Role> dormitoryUserRoles = Arrays.asList(Role.ROLE_DORM_MANAGER, Role.ROLE_DORM_LIFE_MANAGER, Role.ROLE_DORM_ROOMMATE_MANAGER);

        for (User user : userRepository.findByReceiveNotificationTypesContainsAndRoleNotIn(NotificationType.UNI_DORM, dormitoryUserRoles)) {
            UserNotification userNotification = UserNotification.of(user, notification);
            userNotificationRepository.save(userNotification);
            fcmMessageService.sendUnidormAnnouncementNotification(user, title, announcement.getTitle());
        }
    }
}