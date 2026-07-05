package com.example.appcenter_project.domain.weather.service;

import com.example.appcenter_project.domain.weather.dto.WeatherCacheDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<WeatherCacheDto> get(int nx, int ny) {
        String key = "weather:" + nx + ":" + ny;
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.convertValue(result, WeatherCacheDto.class));
    }

    public void put(int nx, int ny, WeatherCacheDto dto) {
        String key = "weather:" + nx + ":" + ny;
        redisTemplate.opsForValue().set(key, dto, Duration.ofSeconds(10_800));
    }
}
