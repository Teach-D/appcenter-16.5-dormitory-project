package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.GroupOrderCacheDto;
import com.example.appcenter_project.dto.GroupOrderCommentListCacheDto;
import com.example.appcenter_project.dto.GroupOrderLikeListCacheDto;
import com.example.appcenter_project.dto.ImageListCacheDto;
import com.example.appcenter_project.dto.cache.GroupOrderCacheDto1;
import com.example.appcenter_project.dto.cache.GroupOrderCommentCacheDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.service.RedisService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.*;

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
                // 이제 모든 컬렉션이 이미 로딩되어 있으므로 LazyInitializationException 발생하지 않음
/*                List<ImageListCacheDto> imageList = order.getImageList().stream()
                        .map(ImageListCacheDto::from)
                        .collect(Collectors.toList());*/
                List<Long> groupOrderLikeUserList = order.getGroupOrderLikeList().stream()
                        .map(groupOrderLike -> groupOrderLike.getUser().getId())
                        .toList();

/*                List<GroupOrderLikeListCacheDto> groupOrderLikeList = order.getGroupOrderLikeList().stream()
                        .map(GroupOrderLikeListCacheDto::fromEntity)
                        .collect(Collectors.toList());*/

                List<ResponseGroupOrderCommentDto> groupOrderCommentDtoList = order.getGroupOrderCommentList().stream()
                        .map(groupOrderComment -> ResponseGroupOrderCommentDto.entityToDto(groupOrderComment, groupOrderComment.getUser()))
                        .toList();

/*                List<GroupOrderCommentListCacheDto> groupOrderCommentList = order.getGroupOrderCommentList().stream()
                        .map(GroupOrderCommentListCacheDto::fromEntity)
                        .collect(Collectors.toList());*/

                ResponseGroupOrderDetailDto responseGroupOrderDetailDto = ResponseGroupOrderDetailDto.detailEntityToDto(order, groupOrderCommentDtoList, groupOrderLikeUserList);
                String key = ORDER_CACHE_KEY + order.getId();
                redisTemplate.opsForValue().set(key, responseGroupOrderDetailDto);
                log.info("Cached as JSON: {}", key);
            } catch (Exception e) {
                log.error("Failed to cache as JSON", e);
            }
        }


/*        for (GroupOrder groupOrdersDelivery : groupOrdersDeliveries) {
            String key = ORDER_CACHE_KEY + groupOrdersDelivery.getId();
            log.info("key = {}", key);
            redisTemplate.opsForValue().set(key, groupOrdersDelivery);
        }*/
/*
        Map<String, GroupOrder> orderMap = groupOrdersDeliveries.stream()
                .collect(Collectors.toMap(
                        order -> String.valueOf(order.getId()),
                        order -> order
                ));
        redisTemplate.opsForValue().set(ORDER_CACHE_KEY, orderMap, Duration.ofMinutes(CACHE_EXPIRE_MINUTES));
 */
    }

    /**
     * 하나의 GroupOrder Delivery를 캐싱
     */
    public void cacheDelivery(GroupOrder groupOrderDelivery) {
        String key = ORDER_CACHE_KEY + groupOrderDelivery.getId();
        Optional<GroupOrder> cachedOrderMap = getCachedOrderMap(groupOrderDelivery.getId());
//        if (cachedOrderMap == null) {
//            redisTemplate.opsForValue().set(key, groupOrderDelivery);
//            return;
//        }

        redisTemplate.opsForValue().set(key, groupOrderDelivery);
    }

    public GroupOrder getCachedDelivery(Long groupOrderId) {
        String key = ORDER_CACHE_KEY + groupOrderId;
        return (GroupOrder) redisTemplate.opsForValue().get(key);
    }

    private Optional<GroupOrder> getCachedOrderMap(Long groupOrderId) {
        String key = ORDER_CACHE_KEY + groupOrderId;
        return ofNullable((GroupOrder) (redisTemplate.opsForValue().get(key)));
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

    // 캐시에서 GroupOrder DTO 조회
    public GroupOrderCacheDto1 getGroupOrderFromCache(Long groupOrderId) {
        try {
            String key = ORDER_CACHE_KEY + groupOrderId;
            Object cachedObject = redisTemplate.opsForValue().get(key);

            if (cachedObject instanceof GroupOrderCacheDto1) {
                return (GroupOrderCacheDto1) cachedObject;
            }

            return null;
        } catch (Exception e) {
            log.error("GroupOrder 캐시 조회 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    // 모든 캐시된 GroupOrder 조회
    public ResponseGroupOrderDetailDto getAllCacheDelivery(Long id) {
        String pattern = ORDER_CACHE_KEY + id.toString();
        return getEntityByPattern(pattern, ResponseGroupOrderDetailDto.class);
    }

    // 캐시에서 GroupOrder 삭제
    public void deleteGroupOrderFromCache(Long groupOrderId) {
        try {
            String key = ORDER_CACHE_KEY + groupOrderId;
            redisTemplate.delete(key);
            log.info("GroupOrder 캐시 삭제 완료: {}", key);
        } catch (Exception e) {
            log.error("GroupOrder 캐시 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
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

}
