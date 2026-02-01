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

        List<UserNotification> notifications = new ArrayList<>(user.getUserNotifications());
        Collections.reverse(notifications);

        return notifications.stream()
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

    public Notification createNotification(RequestNotificationDto requestDto) {
        Notification notification = Notification.from(requestDto);
        notificationRepository.save(notification);
        return notification;
    }

    public void createUserNotification(User user, Notification notification) {
        UserNotification userNotification = UserNotification.of(user, notification);
        userNotificationRepository.save(userNotification);
    }

    public Notification createChatNotification(String senderName, Long chatRoomId, String content) {
        Notification chatNotification = Notification.createChatNotification(senderName, content, chatRoomId);
        notificationRepository.save(chatNotification);
        return chatNotification;
    }

    public Notification createRoommateRequestNotification(String senderName, Long matchingId) {
        String title = "새로운 룸메이트 매칭 요청이 도착했습니다!";
        String body = senderName + "님이 룸메이트 매칭 요청을 보냈습니다.";

        Notification roommateRequestNotification = Notification.createRoommateMatchingNotification(title, body, matchingId);
        notificationRepository.save(roommateRequestNotification);
        return roommateRequestNotification;
    }

    public Notification createRoommateAcceptNotification(String senderName, Long matchingId) {
        String title = "룸메이트 매칭이 완료되었습니다!";
        String body = senderName + "님과 룸메이트가 되었습니다.";

        Notification roommateAcceptNotification = Notification.createRoommateMatchingNotification(title, body, matchingId);
        notificationRepository.save(roommateAcceptNotification);
        return roommateAcceptNotification;
    }

    public Notification createRoommateBoardNotification(String authorName, Long boardId) {
        String title = "새로운 룸메이트 게시글이 올라왔어요!";
        String body = authorName + "님이 룸메이트 게시글을 작성했습니다.";

        Notification roommateBoardNotification = Notification.createRoommateBoardNotification(title, body, boardId);
        notificationRepository.save(roommateBoardNotification);
        return roommateBoardNotification;
    }

    // ========== Private Methods ========== //

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
}