package com.example.appcenter_project.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // 1. 단일 엔티티 조회
    public <T> T getEntity(String key, Class<T> entityClass) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }

            if (value instanceof String) {
                // JSON 문자열인 경우
                return objectMapper.readValue((String) value, entityClass);
            } else {
                // 이미 객체로 저장된 경우
                return objectMapper.convertValue(value, entityClass);
            }
        } catch (Exception e) {
            log.error("Redis에서 엔티티 조회 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // 2. 패턴으로 여러 엔티티 조회
    public <T> List<T> getEntitiesByPattern(String pattern, Class<T> entityClass) {
        List<T> entities = new ArrayList<>();
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    T entity = getEntity(key, entityClass);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Redis에서 패턴 조회 중 오류 발생: {}", e.getMessage());
        }
        return entities;
    }

    // 3. Hash 구조로 저장된 데이터 조회
    public <T> T getHashEntity(String key, Class<T> entityClass) {
        try {
            Object value = redisTemplate.opsForHash().entries(key);
            if (value == null) {
                return null;
            }
            return objectMapper.convertValue(value, entityClass);
        } catch (Exception e) {
            log.error("Redis Hash에서 엔티티 조회 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // 4. 모든 키 조회
    public Set<String> getAllKeys() {
        return redisTemplate.keys("*");
    }

    // 5. 키 존재 여부 확인
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 6. JSON 문자열을 엔티티로 변환
    public <T> T parseJsonToEntity(String jsonString, Class<T> entityClass) {
        try {
            return objectMapper.readValue(jsonString, entityClass);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // 7. 엔티티를 JSON으로 변환
    public String entityToJson(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.error("엔티티를 JSON으로 변환 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    // 8. 엔티티 저장
    public void saveEntity(String key, Object entity) {
        redisTemplate.opsForValue().set(key, entity);
    }

    // 9. TTL과 함께 엔티티 저장
    public void saveEntityWithTTL(String key, Object entity, long timeoutSeconds) {
        redisTemplate.opsForValue().set(key, entity, timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }
}