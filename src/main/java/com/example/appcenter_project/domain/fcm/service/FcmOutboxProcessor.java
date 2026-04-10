package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.OutboxStatus;
import com.example.appcenter_project.domain.fcm.repository.FcmOutboxRepository;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    private static final int BATCH_SIZE = 30;
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

        int totalSuccess = 0;
        int totalFail = 0;
        List<String> permanentFailTokens = new ArrayList<>();

        Map<String, List<FcmOutbox>> groups = candidates.stream()
                .collect(Collectors.groupingBy(o -> o.getTitle() + "\0" + o.getBody()));

        for (List<FcmOutbox> group : groups.values()) {
            String title = group.get(0).getTitle();
            String body = group.get(0).getBody();

            for (int i = 0; i < group.size(); i += BATCH_SIZE) {
                List<FcmOutbox> chunk = group.subList(i, Math.min(i + BATCH_SIZE, group.size()));
                int[] result = sendChunk(chunk, title, body, permanentFailTokens);
                totalSuccess += result[0];
                totalFail += result[1];
            }
        }

        if (!permanentFailTokens.isEmpty()) {
            fcmTokenRepository.deleteAllByTokenIn(permanentFailTokens);
            log.info("FCM 오류 토큰 일괄 삭제: {}건", permanentFailTokens.size());
        }

        if (totalSuccess > 0 || totalFail > 0) {
            recordStats(totalSuccess, totalFail);
            log.info("FcmOutboxProcessor 처리 완료: 성공={}, 실패={}", totalSuccess, totalFail);
        }
    }

    private int[] sendChunk(List<FcmOutbox> chunk, String title, String body, List<String> permanentFailTokens) {
        List<String> tokens = chunk.stream().map(FcmOutbox::getToken).toList();

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
            List<SendResponse> responses = batchResponse.getResponses();
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < responses.size(); i++) {
                SendResponse response = responses.get(i);
                FcmOutbox outbox = chunk.get(i);

                if (response.isSuccessful()) {
                    outbox.markSent();
                    successCount++;
                } else {
                    FirebaseMessagingException e = response.getException();
                    String errorCode = e.getMessagingErrorCode() != null
                            ? e.getMessagingErrorCode().name()
                            : "UNKNOWN";

                    if (PERMANENT_ERROR_CODES.contains(errorCode)) {
                        outbox.markDeadPermanent(errorCode);
                        permanentFailTokens.add(outbox.getToken());
                        log.warn("FCM 영구 오류 (id={}): {}", outbox.getId(), errorCode);
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
                    failCount++;
                }
            }

            log.info("FCM 배치 전송: 성공={}, 실패={} ({}건 중)", successCount, failCount, chunk.size());
            return new int[]{successCount, failCount};

        } catch (FirebaseMessagingException e) {
            log.error("FCM 배치 전송 오류: {}", e.getMessage());
            chunk.forEach(outbox -> outbox.markFailed("BATCH_ERROR", LocalDateTime.now().plusMinutes(5)));
            return new int[]{0, chunk.size()};
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
