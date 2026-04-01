package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AsyncViewCountService {

    private final ConcurrentHashMap<Long, LongAdder> groupOrderCountMap = new ConcurrentHashMap<>();
    private final GroupOrderRepository groupOrderRepository;
    private final RedissonClient redissonClient;

    private static final long LOCK_WAIT_TIME = 5L;
    private static final long LOCK_LEASE_TIME = 3L;
    private static final String LOCK_KEY_PREFIX = "group-order:view-count:";

    public void incrementViewCount(Long groupOrderId) {
        groupOrderCountMap.computeIfAbsent(groupOrderId, k -> new LongAdder())
                .increment();
    }

    @Scheduled(fixedRate = 1000)
    public void flushViewCountDB() {
        if (groupOrderCountMap.isEmpty()) {
            return;
        }

        Map<Long, Long> countMap = new HashMap<>();

        groupOrderCountMap.forEach((groupOrderId, adder) -> {
            long count = adder.sumThenReset();
            countMap.put(groupOrderId, count);
        });

        groupOrderCountMap.entrySet().removeIf(entry -> entry.getValue().longValue() == 0L);

        countMap.forEach((groupOrderId, count) -> {
            RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + groupOrderId);
            boolean locked = false;
            try {
                locked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
                if (locked) {
                    groupOrderRepository.incrementViewCountBy(groupOrderId, count);
                } else {
                    log.warn("[{}] groupOrderId={}, count={}",
                            ErrorCode.GROUP_ORDER_VIEW_COUNT_LOCK_FAILED.getMessage(),
                            groupOrderId, count);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[ViewCount] 락 대기 중 인터럽트 발생 - groupOrderId: {}", groupOrderId, e);
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });
    }
}
