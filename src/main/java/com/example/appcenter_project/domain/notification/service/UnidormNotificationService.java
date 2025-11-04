package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
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
        for (User user : userRepository.findByReceiveNotificationTypesContains(NotificationType.UNI_DORM)) {
            UserNotification userNotification = UserNotification.of(user, notification);
            userNotificationRepository.save(userNotification);

            fcmMessageService.sendUnidormNotification(user, notification.getTitle(), notification.getBody());
        }
    }
}
