package com.example.appcenter_project.global.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CouponDateScheduler {

    private final Random random = new Random();
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY = "coupon_date_time";

//    @Scheduled(cron = "0 0 9 * * MON-FRI")
    @Transactional
    public void setupCouponDateScheduler() {
        LocalTime randomTime = generateRandomTime();

        System.out.println("설정된 쿠폰 발급 시간: " + randomTime);

        // 기존 키 패턴으로 검색하여 모두 삭제
        Set<String> keys = redisTemplate.keys(REDIS_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("기존 Redis 키 삭제 완료: " + keys.size() + "개");
        }

        // 새로운 랜덤 시간을 Redis에 저장
        String timeValue = randomTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        redisTemplate.opsForValue().set(REDIS_KEY, timeValue);

        System.out.println("Redis에 새로운 시간 저장 완료: " + timeValue);
    }

    /**
     * 랜덤 시간을 생성합니다.
     * @return 12시, 13시, 18시, 19시 중 하나와 랜덤 분(0-59)
     */
    private LocalTime generateRandomTime() {
        int[] possibleHours = {12, 13, 18, 19};
        int randomHour = possibleHours[random.nextInt(possibleHours.length)];

        // 0~59분 중 랜덤 선택
        int randomMinute = random.nextInt(60);

        return LocalTime.of(randomHour, randomMinute);
    }

}
