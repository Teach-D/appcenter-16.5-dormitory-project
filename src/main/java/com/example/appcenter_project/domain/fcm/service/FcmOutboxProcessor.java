package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.OutboxStatus;
import com.example.appcenter_project.domain.fcm.repository.FcmOutboxRepository;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class FcmOutboxProcessor {

    private static final String FCM_SUCCESS_KEY_PREFIX = "fcm:success:";
    private static final String FCM_FAIL_KEY_PREFIX = "fcm:fail:";
    private static final List<String> PERMANENT_ERROR_CODES = List.of("UNREGISTERED", "INVALID_ARGUMENT");

    private final FcmOutboxRepository fcmOutboxRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void process() {
        LocalDateTime now = LocalDateTime.now();
        List<FcmOutbox> candidates = fcmOutboxRepository.findByStatusInAndNextRetryAtBefore(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), now
        );

        if (candidates.isEmpty()) {
            return;
        }

        candidates.forEach(FcmOutbox::markProcessing);

        int successCount = 0;
        int failCount = 0;

        for (FcmOutbox outbox : candidates) {
            boolean sent = sendAndUpdateStatus(outbox);
            if (sent) {
                successCount++;
            } else {
                failCount++;
            }
        }

        if (successCount > 0 || failCount > 0) {
            recordStats(successCount, failCount);
            log.info("FcmOutboxProcessor 처리 완료: 성공={}, 실패={}", successCount, failCount);
        }
    }

    private boolean sendAndUpdateStatus(FcmOutbox outbox) {
        Message message = Message.builder()
                .setToken(outbox.getToken())
                .setNotification(Notification.builder()
                        .setTitle(outbox.getTitle())
                        .setBody(outbox.getBody())
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            outbox.markSent();
            return true;
        } catch (FirebaseMessagingException e) {
            String errorCode = e.getMessagingErrorCode() != null
                    ? e.getMessagingErrorCode().name()
                    : "UNKNOWN";

            if (PERMANENT_ERROR_CODES.contains(errorCode)) {
                outbox.markDeadPermanent(errorCode);
                fcmTokenRepository.deleteByToken(outbox.getToken());
                log.warn("FCM 오류 토큰 삭제 (id={}): {}", outbox.getId(), errorCode);
            } else {
                long backoffMinutes = 5L * (1L << outbox.getRetryCount());
                outbox.markFailed(errorCode, LocalDateTime.now().plusMinutes(backoffMinutes));
                if (outbox.isExhausted()) {
                    outbox.markDeadExhausted(errorCode);
                    log.warn("FCM 최대 재시도 초과 (id={}): {}", outbox.getId(), errorCode);
                } else {
                    log.warn("FCM 전송 실패, {}분 후 재시도 (id={}, retry={}/{}): {}",
                            backoffMinutes, outbox.getId(), outbox.getRetryCount(), outbox.getMaxRetry(), errorCode);
                }
            }
            return false;
        }
    }

    private void recordStats(int successCount, int failCount) {
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
}
