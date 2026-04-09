package com.example.appcenter_project.domain.fcm.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * FCM 전송을 fcmExecutor 스레드 풀에서 비동기로 처리하는 컴포넌트.
 * <p>
 * - sendBatch(): 최대 500개 토큰을 sendEachForMulticast()로 HTTP 1회에 전송 (전체 알림용)
 * - sendOne(): 단일 토큰 전송 (개별 사용자 알림용)
 * <p>
 * FcmMessageService 내부에서 @Async를 직접 호출하면 Spring 프록시를 우회해
 * 비동기가 동작하지 않으므로 별도 빈으로 분리했다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FcmAsyncSender {

    private static final int MULTICAST_LIMIT = 500;
    private static final String FCM_SUCCESS_KEY_PREFIX = "fcm:success:";
    private static final String FCM_FAIL_KEY_PREFIX    = "fcm:fail:";

    private final FcmTokenRepository fcmTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 토큰 목록(최대 500개)을 MulticastMessage로 묶어 HTTP 1회에 전송.
     * FcmMessageService에서 500개 단위 청크로 분할 후 호출한다.
     */
    @Async("fcmExecutor")
    @Transactional
    public CompletableFuture<Void> sendBatch(List<String> tokens, String title, String body) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .build();

        try {
            BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM 배치 전송: 성공={}, 실패={}", batchResponse.getSuccessCount(), batchResponse.getFailureCount());

            List<SendResponse> responses = batchResponse.getResponses();
            List<String> failedTokens = new java.util.ArrayList<>();
            int successCount = 0;

            for (int i = 0; i < responses.size(); i++) {
                if (responses.get(i).isSuccessful()) {
                    successCount++;
                } else {
                    String failedToken = tokens.get(i);
                    log.warn("FCM 전송 실패 (token: {}...): {}",
                            failedToken.substring(0, Math.min(20, failedToken.length())),
                            responses.get(i).getException().getMessage());
                    failedTokens.add(failedToken);
                }
            }

            if (!failedTokens.isEmpty()) {
                fcmTokenRepository.deleteAllByTokenIn(failedTokens);
            }
            recordBatch(successCount, failedTokens.size());
        } catch (FirebaseMessagingException e) {
            log.error("FCM 배치 전송 오류: {}", e.getMessage());
            recordFail();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 단일 토큰 전송. 개별 사용자 알림(sendNotification, sendGroupOrderNotification 등)에서 사용.
     */
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

    private void recordBatch(int successCount, int failCount) {
        String today = LocalDate.now().toString();
        if (successCount > 0) {
            String key = FCM_SUCCESS_KEY_PREFIX + today;
            redisTemplate.opsForValue().increment(key, successCount);
            redisTemplate.expire(key, Duration.ofHours(24));
        }
        if (failCount > 0) {
            String key = FCM_FAIL_KEY_PREFIX + today;
            redisTemplate.opsForValue().increment(key, failCount);
            redisTemplate.expire(key, Duration.ofHours(24));
        }
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
