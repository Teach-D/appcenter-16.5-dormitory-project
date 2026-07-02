package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaGuard {

    private static final String USAGE_KEY_PREFIX = "kakao:usage:";
    private static final String QUOTA_ALERT_KEY_PREFIX = "kakao:quota:alerted:";
    private static final Duration USAGE_TTL = Duration.ofDays(2);
    private static final Duration ALERT_TTL = Duration.ofDays(1);

    @Value("${kakao.local.daily-quota-limit:250000}")
    private long quotaLimit;

    private final StringRedisTemplate stringRedisTemplate;
    private final SlackErrorNotifier slackErrorNotifier;

    public boolean isExceeded() {
        String key = USAGE_KEY_PREFIX + LocalDate.now(ZoneId.of("Asia/Seoul"));
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return false;
        }
        long current = Long.parseLong(value);
        if (current >= quotaLimit) {
            sendAlertOnce();
            return true;
        }
        return false;
    }

    public void increment() {
        String key = USAGE_KEY_PREFIX + LocalDate.now(ZoneId.of("Asia/Seoul"));
        Long current = stringRedisTemplate.opsForValue().increment(key);
        if (current != null && current == 1L) {
            stringRedisTemplate.expire(key, USAGE_TTL);
        }
    }

    public long getUsageToday() {
        String key = USAGE_KEY_PREFIX + LocalDate.now(ZoneId.of("Asia/Seoul"));
        String value = stringRedisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    public long getQuotaLimit() {
        return quotaLimit;
    }

    private void sendAlertOnce() {
        String alertKey = QUOTA_ALERT_KEY_PREFIX + LocalDate.now(ZoneId.of("Asia/Seoul"));
        Boolean set = stringRedisTemplate.opsForValue().setIfAbsent(alertKey, "1", ALERT_TTL);
        if (Boolean.TRUE.equals(set)) {
            log.warn("[QuotaGuard] 카카오 API 일일 한도 {}건 도달", quotaLimit);
        }
    }
}
