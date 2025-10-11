package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.ApiType;
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
public class UnidormNotificationService {

    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;

    public void saveAndSendUnidormNotification(Notification notification) {
        for (User user : userRepository.findAll()) {
            UserNotification userNotification = UserNotification.of(user, notification);
            userNotificationRepository.save(userNotification);

            fcmMessageService.sendUnidormNotification(user, notification.getTitle(), notification.getBody());
        }
    }
}
