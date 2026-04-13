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
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class FcmOutboxProcessor {

    private static final int DB_CHUNK_SIZE = 500;
    private static final int FCM_BATCH_SIZE = 30;
    private static final String FCM_SUCCESS_KEY_PREFIX = "fcm:success:";
    private static final String FCM_FAIL_KEY_PREFIX = "fcm:fail:";
    private static final String FCM_DEAD_KEY_PREFIX = "fcm:dead:";
    private static final String FCM_DLQ_ALERT_KEY = "fcm:dlq:alert";
    private static final List<String> PERMANENT_ERROR_CODES = List.of("UNREGISTERED", "INVALID_ARGUMENT");
    private static final int STUCK_THRESHOLD_MINUTES = 5;
    private static final double DLQ_ERROR_RATE_THRESHOLD = 0.05;
    private static final int DLQ_MIN_SAMPLE_SIZE = 100;

    @Value("${slack.webhook-url:}")
    private String slackWebhookUrl;

    private final FcmOutboxRepository fcmOutboxRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TransactionTemplate transactionTemplate;

    private static final int CLEANUP_RETENTION_DAYS = 7;

    private record RetryKey(int retryCount, String errorCode) {}

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(CLEANUP_RETENTION_DAYS);
        int deleted = fcmOutboxRepository.deleteOldOutboxes(
                List.of(OutboxStatus.SENT, OutboxStatus.DEAD_PERMANENT, OutboxStatus.DEAD_EXHAUSTED, OutboxStatus.EXPIRED),
                threshold
        );
        if (deleted > 0) {
            log.info("FCM Outbox 정리 완료: {}건 삭제 ({}일 경과)", deleted, CLEANUP_RETENTION_DAYS);
        }
    }

    @PreDestroy
    public void shutdown() {
        transactionTemplate.executeWithoutResult(status -> {
            int recovered = fcmOutboxRepository.recoverAllProcessing(LocalDateTime.now());
            if (recovered > 0) {
                log.info("Graceful Shutdown: PROCESSING → PENDING 복구 {}건", recovered);
            }
        });
    }

    @Scheduled(fixedDelay = 30_000)
    public void process() {
        LocalDateTime now = LocalDateTime.now();

        int stuck = fcmOutboxRepository.recoverStuckProcessing(now, now.minusMinutes(STUCK_THRESHOLD_MINUTES));
        if (stuck > 0) {
            log.warn("FCM stuck PROCESSING 복구: {}건 ({}분 초과)", stuck, STUCK_THRESHOLD_MINUTES);
        }

        int expired = fcmOutboxRepository.bulkMarkExpired(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), now
        );
        if (expired > 0) {
            log.info("FCM 만료 알림 폐기: {}건", expired);
        }

        Pageable pageable = PageRequest.of(0, DB_CHUNK_SIZE);
        int totalSuccess = 0;
        int totalFail = 0;
        int totalDead = 0;
        List<String> permanentFailTokens = new ArrayList<>();

        while (true) {
            List<FcmOutbox> chunk = fcmOutboxRepository.findChunk(
                    List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), now, pageable
            );
            if (chunk.isEmpty()) break;

            List<Long> chunkIds = chunk.stream().map(FcmOutbox::getId).toList();
            fcmOutboxRepository.bulkMarkProcessing(chunkIds, LocalDateTime.now());

            List<Long> sentIds = new ArrayList<>();
            Map<String, List<Long>> deadPermMap = new HashMap<>();
            Map<String, List<Long>> exhaustedMap = new HashMap<>();
            Map<RetryKey, List<Long>> failedMap = new HashMap<>();

            Map<String, List<FcmOutbox>> groups = chunk.stream()
                    .collect(Collectors.groupingBy(o -> o.getTitle() + "\0" + o.getBody()));

            for (List<FcmOutbox> group : groups.values()) {
                String title = group.get(0).getTitle();
                String body = group.get(0).getBody();

                for (int i = 0; i < group.size(); i += FCM_BATCH_SIZE) {
                    List<FcmOutbox> batch = group.subList(i, Math.min(i + FCM_BATCH_SIZE, group.size()));
                    int[] counts = collectBatch(batch, title, body, sentIds, deadPermMap, exhaustedMap, failedMap, permanentFailTokens);
                    totalSuccess += counts[0];
                    totalFail += counts[1];
                    totalDead += counts[2];
                }
            }

            if (!sentIds.isEmpty()) {
                fcmOutboxRepository.bulkUpdateStatus(sentIds, OutboxStatus.SENT);
            }
            deadPermMap.forEach((errorCode, ids) ->
                    fcmOutboxRepository.bulkUpdateStatusWithError(ids, OutboxStatus.DEAD_PERMANENT, errorCode));
            exhaustedMap.forEach((errorCode, ids) ->
                    fcmOutboxRepository.bulkUpdateStatusWithError(ids, OutboxStatus.DEAD_EXHAUSTED, errorCode));
            failedMap.forEach((key, ids) -> {
                long backoffMinutes = 5L * (1L << key.retryCount());
                fcmOutboxRepository.bulkMarkFailed(ids, key.errorCode(), LocalDateTime.now().plusMinutes(backoffMinutes));
            });
        }

        if (!permanentFailTokens.isEmpty()) {
            fcmTokenRepository.deleteAllByTokenIn(permanentFailTokens);
            log.info("FCM 오류 토큰 일괄 삭제: {}건", permanentFailTokens.size());
        }

        if (totalSuccess > 0 || totalFail > 0) {
            recordStats(totalSuccess, totalFail, totalDead);
            log.info("FcmOutboxProcessor 처리 완료: 성공={}, 실패={}, DEAD={}", totalSuccess, totalFail, totalDead);
        }

        checkDlqAlert();
    }

    private void checkDlqAlert() {
        String today = LocalDate.now().toString();
        long success = getRedisLong(FCM_SUCCESS_KEY_PREFIX + today);
        long fail = getRedisLong(FCM_FAIL_KEY_PREFIX + today);
        long dead = getRedisLong(FCM_DEAD_KEY_PREFIX + today);
        long total = success + fail;

        if (total < DLQ_MIN_SAMPLE_SIZE) return;

        double errorRate = (double) dead / total;
        if (errorRate > DLQ_ERROR_RATE_THRESHOLD && !slackWebhookUrl.isBlank()) {
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(FCM_DLQ_ALERT_KEY, "1", Duration.ofHours(1));
            if (Boolean.TRUE.equals(isNew)) {
                sendSlackAlert(dead, total, errorRate);
            }
        }
    }

    private long getRedisLong(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) return 0L;
        return Long.parseLong(val.toString());
    }

    private void sendSlackAlert(long deadCount, long totalCount, double errorRate) {
        String payload = String.format(
                "{\"text\": \"[FCM DLQ 경고] 오늘 에러율 %.1f%% 초과 (DEAD %d건 / 전체 %d건). 즉시 확인이 필요합니다.\"}",
                errorRate * 100, deadCount, totalCount
        );
        try {
            HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(slackWebhookUrl))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            log.warn("FCM DLQ 경고 Slack 알림 전송: 에러율 {}% (DEAD {}건 / 전체 {}건)",
                    String.format("%.1f", errorRate * 100), deadCount, totalCount);
        } catch (Exception e) {
            log.error("Slack 알림 전송 실패: {}", e.getMessage());
        }
    }

    private int[] collectBatch(List<FcmOutbox> batch, String title, String body,
            List<Long> sentIds, Map<String, List<Long>> deadPermMap,
            Map<String, List<Long>> exhaustedMap, Map<RetryKey, List<Long>> failedMap,
            List<String> permanentFailTokens) {
        List<String> tokens = batch.stream().map(FcmOutbox::getToken).toList();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            List<SendResponse> responses = batchResponse.getResponses();
            int successCount = 0;
            int failCount = 0;
            int deadCount = 0;

            for (int i = 0; i < responses.size(); i++) {
                SendResponse response = responses.get(i);
                FcmOutbox outbox = batch.get(i);

                if (response.isSuccessful()) {
                    sentIds.add(outbox.getId());
                    successCount++;
                } else {
                    FirebaseMessagingException e = response.getException();
                    String errorCode = e.getMessagingErrorCode() != null
                            ? e.getMessagingErrorCode().name()
                            : "UNKNOWN";

                    if (PERMANENT_ERROR_CODES.contains(errorCode)) {
                        deadPermMap.computeIfAbsent(errorCode, k -> new ArrayList<>()).add(outbox.getId());
                        permanentFailTokens.add(outbox.getToken());
                        deadCount++;
                        log.warn("FCM 영구 오류 (id={}): {}", outbox.getId(), errorCode);
                    } else {
                        boolean exhausted = outbox.getRetryCount() + 1 >= outbox.getMaxRetry();
                        if (exhausted) {
                            exhaustedMap.computeIfAbsent(errorCode, k -> new ArrayList<>()).add(outbox.getId());
                            deadCount++;
                            log.warn("FCM 최대 재시도 초과 (id={}): {}", outbox.getId(), errorCode);
                        } else {
                            RetryKey key = new RetryKey(outbox.getRetryCount(), errorCode);
                            failedMap.computeIfAbsent(key, k -> new ArrayList<>()).add(outbox.getId());
                            long backoffMinutes = 5L * (1L << outbox.getRetryCount());
                            log.warn("FCM 전송 실패, {}분 후 재시도 (id={}, retry={}/{}): {}",
                                    backoffMinutes, outbox.getId(), outbox.getRetryCount() + 1, outbox.getMaxRetry(), errorCode);
                        }
                    }
                    failCount++;
                }
            }

            log.info("FCM 배치 전송: 성공={}, 실패={} ({}건 중)", successCount, failCount, batch.size());
            return new int[]{successCount, failCount, deadCount};

        } catch (FirebaseMessagingException e) {
            log.error("FCM 배치 전송 오류: {}", e.getMessage());
            int deadCount = 0;
            for (FcmOutbox outbox : batch) {
                boolean exhausted = outbox.getRetryCount() + 1 >= outbox.getMaxRetry();
                if (exhausted) {
                    exhaustedMap.computeIfAbsent("BATCH_ERROR", k -> new ArrayList<>()).add(outbox.getId());
                    deadCount++;
                } else {
                    failedMap.computeIfAbsent(new RetryKey(outbox.getRetryCount(), "BATCH_ERROR"), k -> new ArrayList<>()).add(outbox.getId());
                }
            }
            return new int[]{0, batch.size(), deadCount};
        }
    }

    private void recordStats(int successCount, int failCount, int deadCount) {
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
        if (deadCount > 0) {
            String key = FCM_DEAD_KEY_PREFIX + today;
            redisTemplate.opsForValue().increment(key, deadCount);
            redisTemplate.expire(key, Duration.ofHours(24));
        }
    }
}
