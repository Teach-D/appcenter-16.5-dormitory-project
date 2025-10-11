package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.ApiType;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

        for (User user : userRepository.findByDormTypeNot(DormType.NONE)) {
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

        for (User user : userRepository.findByDormTypeNot(DormType.NONE)) {
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

        for (User user : userRepository.findAll()) {
            UserNotification userNotification = UserNotification.of(user, notification);
            userNotificationRepository.save(userNotification);
            fcmMessageService.sendUnidormAnnouncementNotification(user, title, announcement.getTitle());
        }
    }
}