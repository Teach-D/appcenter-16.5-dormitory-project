package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.notification.dto.request.RequestNotificationDto;
import com.example.appcenter_project.domain.notification.dto.response.ResponseNotificationDto;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.appcenter_project.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmMessageService fcmMessageService;

    // ========== Public Methods ========== //

    public void saveNotification(RequestNotificationDto requestDto) {
        Notification notification = createNotification(requestDto);

        NotificationType notificationType = NotificationType.from(requestDto.getNotificationType());
        List<User> receiveUsers = userRepository.findByReceiveNotificationTypesContains(notificationType);

        saveUserNotifications(notification, receiveUsers);
        sendFcmMessages(notification, receiveUsers);
    }

    public void saveNotificationByStudentNumber(RequestNotificationDto requestDto, String studentNumber) {
        String title = "유니돔으로부터 알림이 도착했습니다!";

        User user = userRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Notification notification = createNotification(requestDto);
        createUserNotification(user, notification);

        fcmMessageService.sendNotification(user, title, notification.getTitle());
    }

    public ResponseNotificationDto findNotification(Long userId, Long notificationId) {
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(userId, notificationId).orElseThrow();
        return  ResponseNotificationDto.from(userNotification);
    }

    public List<ResponseNotificationDto> findNotificationsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return user.getUserNotifications().stream()
                .map(userNotification -> {
                    userNotification.changeReadStatus(true);
                    return ResponseNotificationDto.from(userNotification);
                })
                .toList();
    }

    public void updateNotification(Long notificationId, RequestNotificationDto requestDto) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.update(requestDto);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // ========== Private Methods ========== //

    private Notification createNotification(RequestNotificationDto requestDto) {
        Notification notification = Notification.from(requestDto);
        notificationRepository.save(notification);
        return notification;
    }

    private void saveUserNotifications(Notification notification, List<User> receiveUsers) {
        List<UserNotification> userNotifications = receiveUsers.stream()
                .map(receiveUser -> UserNotification.of(receiveUser, notification))
                .toList();

        userNotificationRepository.saveAll(userNotifications);
    }

    private void sendFcmMessages(Notification notification, List<User> receiveUsers) {
        receiveUsers.forEach(receiveUser -> {
            fcmMessageService.sendNotification(receiveUser, notification.getTitle(), notification.getBody());
        });
    }

    private void createUserNotification(User user, Notification notification) {
        UserNotification userNotification = UserNotification.of(user, notification);
        userNotificationRepository.save(userNotification);
    }
}