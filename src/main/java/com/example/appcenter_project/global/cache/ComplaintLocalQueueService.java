package com.example.appcenter_project.global.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ComplaintLocalQueueService {

    private final Cache<Long, Long> localQueue = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    public void enqueue(Long userNotificationId) {
        localQueue.put(userNotificationId, userNotificationId);
        log.info("로컬 캐시 큐 적재 완료 - userNotificationId: {}", userNotificationId);
    }

    public Set<Long> drainAll() {
        Map<Long, Long> snapshot = new HashMap<>(localQueue.asMap());
        localQueue.invalidateAll(snapshot.keySet());
        return snapshot.keySet();
    }

    public boolean isEmpty() {
        return localQueue.estimatedSize() == 0;
    }
}
