package com.example.appcenter_project.service.fcm;

import com.example.appcenter_project.repository.user.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmMessageService {

    private final FcmTokenRepository fcmTokenRepository;

    public String sendNotification(String targetToken, String title, String body) {
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
