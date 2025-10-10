package com.example.appcenter_project.service.fcm;

import com.example.appcenter_project.dto.response.fcm.ResponseFcmMessageDto;
import com.example.appcenter_project.entity.user.FcmToken;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.user.FcmTokenRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class FcmMessageService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    // todo User user
    public String sendNotification(User user, String title, String body) {
        for (FcmToken fcmToken : user.getFcmTokenList()) {
            String targetToken = fcmToken.getToken();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent FCM message: {}", response);
                return response; // 메시지 ID 반환
            } catch (Exception e) {
                log.error("Error sending FCM message", e);
                fcmTokenRepository.deleteByToken(targetToken);
                throw new RuntimeException("FCM 발송 실패", e);
            }
        }
        // todo 임시로 null
        return null;
    }

    // 추가: 전체 사용자(회원 + 비회원)에게 전송
    @Transactional
    public ResponseFcmMessageDto sendNotificationToAllUsers(String title, String body) {
        List<FcmToken> allTokens = fcmTokenRepository.findAll();

        if (allTokens.isEmpty()) {
            log.warn("No FCM tokens found. 전체 사용자에게 보낼 토큰이 없습니다.");
            throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
        }

        for (FcmToken fcmToken : allTokens) {
            String targetToken = fcmToken.getToken();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent FCM message to token: {}", response);
            } catch (Exception e) {
                log.error("Error sending FCM message to token: {}", targetToken, e);
                fcmTokenRepository.deleteByToken(targetToken);
                throw new CustomException(ErrorCode.FCM_SEND_FAILED);
            }
        }

        return ResponseFcmMessageDto.builder()
                .messageId("ALL_USERS")
                .status("SUCCESS")
                .build();
    }

    public String sendNotificationDormitoryPerson(String title, String body) {
        for (User user : userRepository.findByDormTypeNot(DormType.NONE)) {
            if (user.getReceiveNotificationTypes().contains(NotificationType.DORMITORY)) {
                for (FcmToken fcmToken : user.getFcmTokenList()) {
                    String targetToken = fcmToken.getToken();

                    Notification notification = Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build();

                    Message message = Message.builder()
                            .setToken(targetToken)
                            .setNotification(notification)
                            .build();

                    try {
                        String response = FirebaseMessaging.getInstance().send(message);
                        log.info("Successfully sent FCM message: {}", response);
                        return response; // 메시지 ID 반환
                    } catch (Exception e) {
                        log.error("Error sending FCM message", e);
                        fcmTokenRepository.deleteByToken(targetToken);
                        throw new RuntimeException("FCM 발송 실패", e);
                    }
                }
            }
        }

        // todo 임시로 null
        return null;
    }

    public void sendGroupOrderNotification(User user, String title, String body) {
        if (!user.getReceiveNotificationTypes().contains(NotificationType.GROUP_ORDER)) {
            return;
        }

        sendMessageToUser(user, title, body);
    }

    public void sendDormitoryNotification(User user, String title, String body) {
        if (!user.getReceiveNotificationTypes().contains(NotificationType.DORMITORY)) {
            return;
        }

        sendMessageToUser(user, title, body);

    }

    public void sendUnidormNotification(User user, String title, String body) {
        if (!user.getReceiveNotificationTypes().contains(NotificationType.UNI_DORM)) {
            return;
        }

        sendMessageToUser(user, title, body);
    }

    private void sendMessageToUser(User user, String title, String body) {
        for (FcmToken fcmToken : user.getFcmTokenList()) {
            String targetToken = fcmToken.getToken();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .build();

            try {
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent FCM message: {}", response);
            } catch (Exception e) {
                log.error("Error sending FCM message", e);
                fcmTokenRepository.deleteByToken(targetToken);
                throw new RuntimeException("FCM 발송 실패", e);
            }
        }
    }

    public void sendSupportersNotification(User user, String title, String body) {
        if (!user.getReceiveNotificationTypes().contains(NotificationType.SUPPORTERS)) {
            return;
        }

        sendMessageToUser(user, title, body);
    }

    public void sendUnidormAnnouncementNotification(User user, String title, String body) {
        if (!user.getReceiveNotificationTypes().contains(NotificationType.UNI_DORM)) {
            return;
        }

        sendMessageToUser(user, title, body);
    }
}
