package com.example.appcenter_project.domain.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * FCM 토큰 1개를 fcmExecutor 스레드 풀에서 비동기로 전송하는 컴포넌트.
 * <p>
 * FcmMessageService 내부에서 @Async 메서드를 호출하면 Spring 프록시를 우회해
 * 비동기가 동작하지 않는 문제를 피하기 위해 별도 빈으로 분리했다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FcmAsyncSender {

    private static final String FCM_SUCCESS_KEY_PREFIX = "fcm:success:";
    private static final String FCM_FAIL_KEY_PREFIX    = "fcm:fail:";

    private final FcmTokenRepository fcmTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Async("fcmExecutor")
    @Transactional
    public CompletableFuture<Void> sendOne(String token, String title, String body) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: {}", response);
            recordSuccess();
        } catch (Exception e) {
            log.error("FCM 전송 실패 (token: {}...): {}", token.substring(0, Math.min(20, token.length())), e.getMessage());
            fcmTokenRepository.deleteByToken(token);
            recordFail();
        }

        return CompletableFuture.completedFuture(null);
    }

    private void recordSuccess() {
        String key = FCM_SUCCESS_KEY_PREFIX + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(24));
    }

    private void recordFail() {
        String key = FCM_FAIL_KEY_PREFIX + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(24));
    }
}
