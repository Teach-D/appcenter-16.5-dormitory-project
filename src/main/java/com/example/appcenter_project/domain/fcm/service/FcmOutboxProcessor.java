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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class FcmOutboxProcessor {

    private static final int DB_CHUNK_SIZE = 500;
    private static final int FCM_BATCH_SIZE = 30;
    private static final String FCM_SUCCESS_KEY_PREFIX = "fcm:success:";
    private static final String FCM_FAIL_KEY_PREFIX = "fcm:fail:";
    private static final List<String> PERMANENT_ERROR_CODES = List.of("UNREGISTERED", "INVALID_ARGUMENT");

    private final FcmOutboxRepository fcmOutboxRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private record RetryKey(int retryCount, String errorCode) {}

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void process() {
        Pageable pageable = PageRequest.of(0, DB_CHUNK_SIZE);
        int totalSuccess = 0;
        int totalFail = 0;
        List<String> permanentFailTokens = new ArrayList<>();

        while (true) {
            List<FcmOutbox> chunk = fcmOutboxRepository.findChunk(
                    List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), LocalDateTime.now(), pageable
            );
            if (chunk.isEmpty()) break;

            List<Long> chunkIds = chunk.stream().map(FcmOutbox::getId).toList();
            fcmOutboxRepository.bulkMarkProcessing(chunkIds);

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
            recordStats(totalSuccess, totalFail);
            log.info("FcmOutboxProcessor 처리 완료: 성공={}, 실패={}", totalSuccess, totalFail);
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
                        log.warn("FCM 영구 오류 (id={}): {}", outbox.getId(), errorCode);
                    } else {
                        boolean exhausted = outbox.getRetryCount() + 1 >= outbox.getMaxRetry();
                        if (exhausted) {
                            exhaustedMap.computeIfAbsent(errorCode, k -> new ArrayList<>()).add(outbox.getId());
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
            return new int[]{successCount, failCount};

        } catch (FirebaseMessagingException e) {
            log.error("FCM 배치 전송 오류: {}", e.getMessage());
            for (FcmOutbox outbox : batch) {
                boolean exhausted = outbox.getRetryCount() + 1 >= outbox.getMaxRetry();
                if (exhausted) {
                    exhaustedMap.computeIfAbsent("BATCH_ERROR", k -> new ArrayList<>()).add(outbox.getId());
                } else {
                    failedMap.computeIfAbsent(new RetryKey(outbox.getRetryCount(), "BATCH_ERROR"), k -> new ArrayList<>()).add(outbox.getId());
                }
            }
            return new int[]{0, batch.size()};
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
