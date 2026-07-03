package com.example.appcenter_project.domain.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceCacheService {

    private static final String PLACE_KEY_PREFIX = "kakao:place:";
    private static final String HIT_KEY_PREFIX = "kakao:cache:hit:";
    private static final String MISS_KEY_PREFIX = "kakao:cache:miss:";
    private static final String HIT_TOTAL_KEY = "kakao:cache:hit";
    private static final String MISS_TOTAL_KEY = "kakao:cache:miss";
    private static final String NOT_FOUND_SENTINEL = "__NOT_FOUND__";
    private static final Duration PLACE_TTL = Duration.ofHours(24);
    private static final Duration COUNTER_TTL = Duration.ofDays(30);

    private final StringRedisTemplate stringRedisTemplate;

    public Optional<String> get(String normalizedKey) {
        String value = stringRedisTemplate.opsForValue().get(PLACE_KEY_PREFIX + normalizedKey);
        return Optional.ofNullable(value);
    }

    public void put(String normalizedKey, String placeId) {
        stringRedisTemplate.opsForValue().set(PLACE_KEY_PREFIX + normalizedKey, placeId, PLACE_TTL);
    }

    public void putNotFound(String normalizedKey) {
        stringRedisTemplate.opsForValue().set(PLACE_KEY_PREFIX + normalizedKey, NOT_FOUND_SENTINEL, PLACE_TTL);
    }

    public boolean isNotFoundSentinel(String value) {
        return NOT_FOUND_SENTINEL.equals(value);
    }

    public String getNotFoundSentinel() {
        return NOT_FOUND_SENTINEL;
    }

    public void incrementHitCounter() {
        String date = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();
        stringRedisTemplate.opsForValue().increment(HIT_KEY_PREFIX + date);
        stringRedisTemplate.expire(HIT_KEY_PREFIX + date, COUNTER_TTL);
        stringRedisTemplate.opsForValue().increment(HIT_TOTAL_KEY);
    }

    public void incrementMissCounter() {
        String date = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();
        stringRedisTemplate.opsForValue().increment(MISS_KEY_PREFIX + date);
        stringRedisTemplate.expire(MISS_KEY_PREFIX + date, COUNTER_TTL);
        stringRedisTemplate.opsForValue().increment(MISS_TOTAL_KEY);
    }

    public void incrementFallbackCounter() {
        String date = LocalDate.now(ZoneId.of("Asia/Seoul")).toString();
        String key = "kakao:fallback:" + date;
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, COUNTER_TTL);
    }

    public Long getCacheHitTotal() {
        String value = stringRedisTemplate.opsForValue().get(HIT_TOTAL_KEY);
        return value == null ? 0L : Long.parseLong(value);
    }

    public Long getCacheMissTotal() {
        String value = stringRedisTemplate.opsForValue().get(MISS_TOTAL_KEY);
        return value == null ? 0L : Long.parseLong(value);
    }

    public Long getDailyHits(String date) {
        String value = stringRedisTemplate.opsForValue().get(HIT_KEY_PREFIX + date);
        return value == null ? null : Long.parseLong(value);
    }

    public Long getDailyMisses(String date) {
        String value = stringRedisTemplate.opsForValue().get(MISS_KEY_PREFIX + date);
        return value == null ? null : Long.parseLong(value);
    }

    public Long getDailyFallbacks(String date) {
        String value = stringRedisTemplate.opsForValue().get("kakao:fallback:" + date);
        return value == null ? null : Long.parseLong(value);
    }
}
