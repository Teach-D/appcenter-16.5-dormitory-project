package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.place.controller.dto.ResponseMigrationResultDto;
import com.example.appcenter_project.domain.place.enums.NormalizationOutcome;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceMigrationService {

    private static final String MIGRATION_LOCK_KEY = "places:migration:running";
    private static final Duration MIGRATION_LOCK_TTL = Duration.ofMinutes(30);

    private final GroupOrderRepository groupOrderRepository;
    private final PlaceResolver placeResolver;
    private final QuotaGuard quotaGuard;
    private final StringRedisTemplate stringRedisTemplate;
    private final PlaceGroupOrderUpdateService placeGroupOrderUpdateService;

    public ResponseMigrationResultDto migrate(int maxRows, int chunkSize, int throttleMs) {
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(MIGRATION_LOCK_KEY, "1", MIGRATION_LOCK_TTL);
        if (!Boolean.TRUE.equals(locked)) {
            throw new CustomException(ErrorCode.PLACE_MIGRATION_ALREADY_RUNNING);
        }

        LocalDateTime startedAt = LocalDateTime.now();
        int scannedRows = 0;
        int matchedRows = 0;
        int notFoundRows = 0;
        int errorRows = 0;
        Long quotaExceededAtRow = null;
        long lastProcessedId = 0L;

        try {
            while (scannedRows < maxRows) {
                if (quotaGuard.isExceeded()) {
                    quotaExceededAtRow = (long) scannedRows;
                    log.warn("[Migration] 일일 한도 도달로 중단. scannedRows={}", scannedRows);
                    break;
                }

                List<GroupOrder> chunk = groupOrderRepository.findUnnormalizedFoodOrders(chunkSize, lastProcessedId);
                if (chunk.isEmpty()) {
                    break;
                }

                for (GroupOrder order : chunk) {
                    if (scannedRows >= maxRows) {
                        break;
                    }
                    if (quotaGuard.isExceeded()) {
                        quotaExceededAtRow = (long) scannedRows;
                        break;
                    }

                    scannedRows++;
                    lastProcessedId = order.getId();

                    String keyword = order.getRawPlaceName() != null ? order.getRawPlaceName() : order.getTitle();
                    if (keyword == null || keyword.isBlank()) {
                        errorRows++;
                        continue;
                    }

                    try {
                        PlaceResolveResult result = placeResolver.resolve(null, keyword, GroupOrderType.FOOD);

                        if (result.getOutcome() == NormalizationOutcome.MATCHED && result.getPlace() != null) {
                            placeGroupOrderUpdateService.updatePlace(order.getId(), result.getPlace(), order.getRawPlaceName());
                            matchedRows++;
                        } else if (result.getOutcome() == NormalizationOutcome.NOT_FOUND) {
                            notFoundRows++;
                        } else if (result.getOutcome() == NormalizationOutcome.QUOTA_EXCEEDED) {
                            quotaExceededAtRow = (long) scannedRows;
                            break;
                        } else {
                            errorRows++;
                        }
                    } catch (Exception e) {
                        log.warn("[Migration] row {} 처리 실패: {}", order.getId(), e.getMessage());
                        errorRows++;
                    }

                    if (throttleMs > 0) {
                        sleep(throttleMs);
                    }
                }

                log.info("[Migration] 진행률: scanned={}, matched={}, lastId={}", scannedRows, matchedRows, lastProcessedId);

                if (quotaExceededAtRow != null) {
                    break;
                }
            }
        } finally {
            stringRedisTemplate.delete(MIGRATION_LOCK_KEY);
        }

        return ResponseMigrationResultDto.of(
                startedAt, LocalDateTime.now(),
                scannedRows, matchedRows, notFoundRows, errorRows,
                quotaExceededAtRow, lastProcessedId
        );
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
