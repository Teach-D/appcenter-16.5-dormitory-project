package com.example.appcenter_project.utils;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.service.groupOrder.DeliveryCacheService;
import com.example.appcenter_project.service.groupOrder.GroupOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MealTimeDeliveryScheduler {

    private final GroupOrderService groupOrderService;
    private final DeliveryCacheService deliveryCacheService;

    // 점심시간 시작 (12:00)
    @Scheduled(cron = "0 0 12 * * ?")
    public void onLunchTimeStart() {
        preloadOrdersToCache();
    }

    // 저녁시간 시작 (18:00) - cron 표현식 수정
    @Scheduled(cron = "0 0 18 * * ?")
    public void onDinnerTimeStart() {
        preloadOrdersToCache();
    }

    // 점심시간 종료 (14:00)
    @Scheduled(cron = "0 0 14 * * ?")
    public void onLunchTimeEnd() {
        clearOrderCache();
    }

    // 저녁시간 종료 (20:00) - cron 표현식 수정
    @Scheduled(cron = "0 0 20 * * ?")
    public void onDinnerTimeEnd() {
        clearOrderCache();
    }

    /**
     * GroupOrder 중 모든 Delivery를 캐싱
     */
    private void preloadOrdersToCache() {
        try {
            log.info("schedule start");
            // 모든 주문을 하나의 키에 캐시
            List<GroupOrder> groupOrdersDeliveries = groupOrderService.findGroupOrdersDelivery();
            deliveryCacheService.cacheAllDeliveries(groupOrdersDeliveries);

            log.info("식사시간 시작: 주문 데이터를 Redis에 캐시했습니다. (총 {}개 주문)", groupOrdersDeliveries.size());
        } catch (Exception e) {
            log.error(String.valueOf(e));
        }
    }

    /**
     * 캐싱되어 있는 모든 Delivery를 redis에서 삭제
     */
    private void clearOrderCache() {
        try {
            deliveryCacheService.evictAllDeliveryCaches();
            log.info("식사시간 종료: Redis 캐시를 정리했습니다.");
        } catch (Exception e) {
        }
    }
}
