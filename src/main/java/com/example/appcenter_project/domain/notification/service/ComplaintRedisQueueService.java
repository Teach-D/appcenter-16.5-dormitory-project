package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.global.cache.ComplaintLocalQueueService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintRedisQueueService {

    private static final String QUEUE_KEY_PREFIX = "complaint_queue:user_notification:";
    private static final String CIRCUIT_BREAKER_NAME = "redis-complaint-queue";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ComplaintLocalQueueService localQueueService;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "enqueueToLocalCache")
    public void enqueue(Long userNotificationId) {
        redisTemplate.opsForValue().set(QUEUE_KEY_PREFIX + userNotificationId, userNotificationId);
    }

    public void enqueueToLocalCache(Long userNotificationId, Exception e) {
        log.warn("Redis 장애 감지 - 로컬 캐시 Fallback 적용. userNotificationId: {}, 원인: {}",
                userNotificationId, e.getMessage());
        localQueueService.enqueue(userNotificationId);
    }
}
