package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.mapper.GroupOrderMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GroupOrderLockFacade {
    private final GroupOrderMapper groupOrderMapper;
    private final RedissonClient redissonClient;

    public void plusGroupOrderViewCount(Long groupOrderId) {
        // 락 획득
        RLock lock = redissonClient.getLock(String.format("group_order:click:%d", groupOrderId));
        try {
            // 락 획득에 성공하면
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if (!available) {
                throw new IllegalArgumentException();
            }
            // 게시글 조회 & 조회수 +1
            groupOrderMapper.plusViewCount(groupOrderId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }
}