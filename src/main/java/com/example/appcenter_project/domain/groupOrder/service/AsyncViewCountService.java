package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Service
@RequiredArgsConstructor
@Transactional
public class AsyncViewCountService {

    private final ConcurrentHashMap<Long, LongAdder> groupOrderCountMap = new ConcurrentHashMap<>();
    private final GroupOrderRepository groupOrderRepository;

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

        countMap.forEach((groupOrderId, adder) -> {
            groupOrderRepository.incrementViewCountBy(groupOrderId, adder);
        });
    }
}
