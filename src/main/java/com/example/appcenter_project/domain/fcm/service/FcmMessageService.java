package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmMessageDto;
import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
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

/*    private void sendMessageToUser(User user, String title, String body) {
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
    }*/

    private void sendMessageToUser(User user, String title, String body) {
        log.info("      🚀 sendMessageToUser 시작 (User ID: {})", user.getId());
        log.info("      📱 FCM Token 리스트 크기: {}", user.getFcmTokenList().size());

        int tokenIndex = 0;
        for (FcmToken fcmToken : user.getFcmTokenList()) {
            tokenIndex++;
            String targetToken = fcmToken.getToken();

            log.info("      ━━━ Token [{}/{}] ━━━", tokenIndex, user.getFcmTokenList().size());
            log.info("      Token: {}...", targetToken.substring(0, Math.min(30, targetToken.length())));

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
                log.info("      ✅ FCM 전송 성공: {}", response);
            } catch (Exception e) {
                log.error("      ❌ FCM 전송 실패", e);
                fcmTokenRepository.deleteByToken(targetToken);
            }
        }

        log.info("      🚀 sendMessageToUser 종료 (User ID: {}, 총 {}개 토큰 처리)", user.getId(), tokenIndex);
    }

    public void sendSupporterNotification(User user, String title, String body) {
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