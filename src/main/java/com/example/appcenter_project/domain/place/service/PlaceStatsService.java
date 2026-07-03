package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.place.controller.dto.ResponsePlaceStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceStatsService {

    private final GroupOrderRepository groupOrderRepository;
    private final PlaceCacheService placeCacheService;
    private final QuotaGuard quotaGuard;

    @Transactional(readOnly = true)
    public ResponsePlaceStatsDto getStats(int days) {
        long distinctRaw = groupOrderRepository.countDistinctRawPlaceName();
        long distinctPlaceId = groupOrderRepository.countDistinctPlaceId();

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        long newOrders = groupOrderRepository.countByCreatedAtAfter(since);
        long newOrdersNullPlace = groupOrderRepository.countByCreatedAtAfterAndPlaceIsNull(since);

        long cacheHitTotal = placeCacheService.getCacheHitTotal();
        long cacheMissTotal = placeCacheService.getCacheMissTotal();
        long quotaUsageToday = quotaGuard.getUsageToday();
        long quotaLimit = quotaGuard.getQuotaLimit();

        ResponsePlaceStatsDto.Summary summary = new ResponsePlaceStatsDto.Summary(
                distinctRaw, distinctPlaceId,
                newOrders, newOrdersNullPlace,
                cacheHitTotal, cacheMissTotal,
                quotaUsageToday, quotaLimit
        );

        List<ResponsePlaceStatsDto.DailyStats> daily = buildDailyStats(days);

        return ResponsePlaceStatsDto.of(summary, daily);
    }

    private List<ResponsePlaceStatsDto.DailyStats> buildDailyStats(int days) {
        List<ResponsePlaceStatsDto.DailyStats> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            Long hits = placeCacheService.getDailyHits(date);
            Long misses = placeCacheService.getDailyMisses(date);
            Long fallbacks = placeCacheService.getDailyFallbacks(date);

            if (hits == null && misses == null && fallbacks == null) {
                continue;
            }

            long kakaoCallsForDay = misses == null ? 0L : misses;
            result.add(new ResponsePlaceStatsDto.DailyStats(
                    date,
                    kakaoCallsForDay,
                    hits == null ? 0L : hits,
                    misses == null ? 0L : misses,
                    fallbacks == null ? 0L : fallbacks
            ));
        }
        return result;
    }
}
