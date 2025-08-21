package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupOrderLockService {

    private final RedissonClient redissonClient;
    private final GroupOrderRepository groupOrderRepository;

    public void increaseGroupOrderViewCount(Long groupOrderId) {
        // 락 획득
        RLock lock = redissonClient.getLock(String.format("group-order-click:%d", groupOrderId));

        try {
            // 락 획득에 성공하면
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if (!available) {
                throw new IllegalArgumentException();
            }
            // 게시글 조회 & 조회수 +1
            groupOrderRepository.plusViewCount(groupOrderId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
}
