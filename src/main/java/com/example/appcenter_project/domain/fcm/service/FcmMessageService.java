package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmMessageDto;
import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmStatsDto;
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
import java.time.LocalDate;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class FcmMessageService {

    private static final String FCM_SUCCESS_KEY_PREFIX = "fcm:success:";
    private static final String FCM_FAIL_KEY_PREFIX = "fcm:fail:";

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Async("fcmExecutor")
    public void sendNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        for (FcmToken fcmToken : fcmTokenRepository.findAllByUser(freshUser)) {
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
                recordFcmSuccess();
            } catch (Exception e) {
                log.error("Error sending FCM message", e);
                fcmTokenRepository.deleteByToken(targetToken);
                recordFcmFail();
            }
        }
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
                recordFcmSuccess();
            } catch (Exception e) {
                log.error("Error sending FCM message to token: {}", targetToken, e);
                fcmTokenRepository.deleteByToken(targetToken);
                recordFcmFail();
            }
        }

        return ResponseFcmMessageDto.builder()
                .messageId("ALL_USERS")
                .status("SUCCESS")
                .build();
    }

    @Async("fcmExecutor")
    public void sendNotificationDormitoryPerson(String title, String body) {
        List<User> dormitoryUsers = userRepository.findDormitoryUsersWithFcmTokens(DormType.NONE, NotificationType.DORMITORY);
        for (User user : dormitoryUsers) {
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
                    recordFcmSuccess();
                } catch (Exception e) {
                    log.error("Error sending FCM message", e);
                    fcmTokenRepository.deleteByToken(targetToken);
                    recordFcmFail();
                }
            }
        }
    }

    @Async("fcmExecutor")
    public void sendGroupOrderNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.GROUP_ORDER)) {
            return;
        }

        sendMessageToUser(freshUser, title, body);
    }

    @Async("fcmExecutor")
    public void sendDormitoryNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.DORMITORY)) {
            return;
        }

        sendMessageToUser(freshUser, title, body);
    }

    @Async("fcmExecutor")
    public void sendUnidormNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.UNI_DORM)) {
            return;
        }

        sendMessageToUser(freshUser, title, body);
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
        List<FcmToken> fcmTokens = fcmTokenRepository.findAllByUser(user);
        log.info("      📱 FCM Token 리스트 크기: {}", fcmTokens.size());

        int tokenIndex = 0;
        for (FcmToken fcmToken : fcmTokens) {
            tokenIndex++;
            String targetToken = fcmToken.getToken();

            log.info("      ━━━ Token [{}/{}] ━━━", tokenIndex, fcmTokens.size());
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
                recordFcmSuccess();
            } catch (Exception e) {
                log.error("      ❌ FCM 전송 실패", e);
                fcmTokenRepository.deleteByToken(targetToken);
                recordFcmFail();
            }
        }

        log.info("      🚀 sendMessageToUser 종료 (User ID: {}, 총 {}개 토큰 처리)", user.getId(), tokenIndex);
    }

    @Transactional(readOnly = true)
    public ResponseFcmStatsDto getFcmStats() {
        LocalDate today = LocalDate.now();
        String successKey = FCM_SUCCESS_KEY_PREFIX + today;
        String failKey = FCM_FAIL_KEY_PREFIX + today;

        Object successVal = redisTemplate.opsForValue().get(successKey);
        Object failVal = redisTemplate.opsForValue().get(failKey);

        long successCount = successVal != null ? Long.parseLong(successVal.toString()) : 0L;
        long failCount = failVal != null ? Long.parseLong(failVal.toString()) : 0L;

        return ResponseFcmStatsDto.builder()
                .date(today.toString())
                .successCount(successCount)
                .failCount(failCount)
                .build();
    }

    private void recordFcmSuccess() {
        String key = FCM_SUCCESS_KEY_PREFIX + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(24));
    }

    private void recordFcmFail() {
        String key = FCM_FAIL_KEY_PREFIX + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(24));
    }

    @Async("fcmExecutor")
    public void sendSupporterNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.SUPPORTERS)) {
            return;
        }

        sendMessageToUser(freshUser, title, body);
    }

    @Async("fcmExecutor")
    public void sendUnidormAnnouncementNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.UNI_DORM)) {
            return;
        }

        sendMessageToUser(freshUser, title, body);
    }
}