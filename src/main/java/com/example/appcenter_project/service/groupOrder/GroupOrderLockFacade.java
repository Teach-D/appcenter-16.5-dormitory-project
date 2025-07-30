package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.mapper.GroupOrderMapper;
import com.example.appcenter_project.utils.MealTimeChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupOrderLockFacade {

    private final GroupOrderMapper groupOrderMapper;
    private final RedissonClient redissonClient;
    private final RedisTemplate redisTemplate;

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
            groupOrderMapper.plusViewCount(groupOrderId, 1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    public void plusGroupOrderScheduledViewCount(Long groupOrderId) {
        String key = "group_order_view:" + groupOrderId.toString();

        log.info("redis에 저장 : " + key);

        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, 1);
        } else {
            redisTemplate.opsForValue().increment(key);
        }
    }

    /**
     * 식사시간마다 redis에서 mysql로 조회수 증가
     */
    @Scheduled(cron = "0 * 12-14,18-23 * * *")
    @Transactional
    public void combineGroupOrderViewCount() {
        // Redis에서 group_order_view: 패턴의 모든 키 조회
        Set<String> keys = redisTemplate.keys("group_order_view:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                // 키에서 groupOrderId 추출
                String groupOrderIdStr = key.replace("group_order_view:", "");
                Long groupOrderId = Long.valueOf(groupOrderIdStr);

                // Redis에서 조회수 값 가져오기
                Object viewCountObj = redisTemplate.opsForValue().get(key);
                if (viewCountObj != null) {
                    Integer viewCount = Integer.valueOf(viewCountObj.toString());

                    // MySQL에 조회수 업데이트 (viewCount만큼 증가)
                    groupOrderMapper.plusViewCount(groupOrderId, viewCount);
                    log.info("redis에서 mysql로 증가 groupOrderId : {} viewCount : {}", groupOrderId, viewCount);
                    // Redis에서 해당 키 삭제 (동기화 완료)
                    redisTemplate.delete(key);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid key format: {}", key);
            } catch (Exception e) {
                log.error("Error processing key: {}, error: {}", key, e.getMessage());
            }
        }
    }
}