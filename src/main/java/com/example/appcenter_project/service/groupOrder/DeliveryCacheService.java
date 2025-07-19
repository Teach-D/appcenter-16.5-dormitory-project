package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.service.RedisService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class DeliveryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    private static final String ORDER_CACHE_KEY = "deliverys:"; // 모든 배달을 저장할 단일 키
    private static final int CACHE_EXPIRE_MINUTES = 120; // 2시간

    public DeliveryCacheService(RedisTemplate<String, Object> redisTemplate, RedisService redisService) {
        this.redisTemplate = redisTemplate;
        this.redisService = redisService;
        this.objectMapper = new ObjectMapper();
        // Java 8 Time 모듈 등록
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 식사시간이면 GroupOrder의 모든 Delivery가 캐싱
     */
    @Transactional(readOnly = true)
    public void cacheAllDeliveries(List<GroupOrder> groupOrdersDeliveries) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        for (GroupOrder order : groupOrdersDeliveries) {
            try {
                List<Long> groupOrderLikeUserList = order.getGroupOrderLikeList().stream()
                        .map(groupOrderLike -> groupOrderLike.getUser().getId())
                        .toList();

                List<ResponseGroupOrderCommentDto> groupOrderCommentDtoList = order.getGroupOrderCommentList().stream()
                        .map(groupOrderComment -> ResponseGroupOrderCommentDto.entityToDto(groupOrderComment, groupOrderComment.getUser()))
                        .toList();

                ResponseGroupOrderDetailDto responseGroupOrderDetailDto = ResponseGroupOrderDetailDto.detailEntityToDto(order, groupOrderCommentDtoList, groupOrderLikeUserList);
                String key = ORDER_CACHE_KEY + order.getId();
                redisTemplate.opsForValue().set(key, responseGroupOrderDetailDto);
                log.info("Cached as JSON: {}", key);
            } catch (Exception e) {
                log.error("Failed to cache as JSON", e);
            }
        }
    }

    /**
     * 하나의 GroupOrder Delivery 캐시 삭제
     */
    public void evictDelivery(Long groupOrderId) {
        String key = ORDER_CACHE_KEY + groupOrderId;
        redisTemplate.delete(key);
    }

    /**
     * 모든 GroupOrder Delivery 캐시 삭제
     */
    public void evictAllDeliveryCaches() {
        for (String key : redisTemplate.keys(ORDER_CACHE_KEY + "*")) {
            redisTemplate.delete(key);
        }
    }

    public boolean existsGroupOrderInCache(Long groupOrderId) {
        try {
            String key = ORDER_CACHE_KEY + groupOrderId;
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("GroupOrder 캐시 키 존재 여부 확인 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    // 모든 캐시된 GroupOrder 조회
    public ResponseGroupOrderDetailDto getAllCacheDelivery(Long id) {
        String pattern = ORDER_CACHE_KEY + id.toString();
        return getEntityByPattern(pattern, ResponseGroupOrderDetailDto.class);
    }

    public <T> T getEntityByPattern(String pattern, Class<T> entityClass) {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                // 첫 번째 키만 사용
                String key = keys.iterator().next();
                try {
                    Object cachedObject = redisTemplate.opsForValue().get(key);
                    log.info("cachedObject = {}", cachedObject);

                    if (cachedObject instanceof LinkedHashMap) {
                        return mapper.convertValue(cachedObject, entityClass);
                    } else {
                        log.warn("Unexpected type for key {}: {}", key, cachedObject.getClass().getName());
                    }
                } catch (Exception e) {
                    log.warn("Entity conversion failed for key {}: {}", key, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Redis pattern scan error: {}", e.getMessage(), e);
        }

        return null;
    }

    public ResponseGroupOrderDetailDto updateCacheDelivery(Long groupOrderId, RequestGroupOrderDto requestGroupOrderDto) {
        String key = ORDER_CACHE_KEY + groupOrderId;
        ResponseGroupOrderDetailDto allCacheDelivery = getAllCacheDelivery(groupOrderId);
        ResponseGroupOrderDetailDto responseGroupOrderDetailDto = ResponseGroupOrderDetailDto.updateDto(requestGroupOrderDto);
        responseGroupOrderDetailDto.setGroupOrderCommentDtoList(allCacheDelivery.getGroupOrderCommentDtoList());

        redisTemplate.opsForValue().set(key, responseGroupOrderDetailDto);
        return responseGroupOrderDetailDto;
    }
}
