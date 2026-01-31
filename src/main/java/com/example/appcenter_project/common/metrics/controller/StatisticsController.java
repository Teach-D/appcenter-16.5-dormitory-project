package com.example.appcenter_project.common.metrics.controller;

import com.example.appcenter_project.common.metrics.dto.ApiStatisticsResponse;
import com.example.appcenter_project.common.metrics.service.ApiCallStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Statistics", description = "API 호출 통계")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final ApiCallStatisticsService statisticsService;

    @Operation(summary = "특정 날짜의 API 호출 통계 조회")
    @GetMapping
    public ResponseEntity<List<ApiStatisticsResponse>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String apiPath
    ) {
        // 단일 날짜 조회
        if (date != null) {
            return ResponseEntity.ok(statisticsService.getStatisticsByDate(date));
        }

        // 기간 조회 (기본값: 최근 7일)
        LocalDate endDate = to != null ? to : LocalDate.now();
        LocalDate startDate = from != null ? from : endDate.minusDays(7);

        // 특정 API 경로 필터링
        if (apiPath != null && !apiPath.isBlank()) {
            return ResponseEntity.ok(
                    statisticsService.getStatisticsByApiPath(apiPath, startDate, endDate)
            );
        }

        return ResponseEntity.ok(
                statisticsService.getStatisticsByDateRange(startDate, endDate)
        );
    }

    @Operation(summary = "특정 API의 총 호출 횟수 조회")
    @GetMapping("/total")
    public ResponseEntity<Long> getTotalCallCount(
            @RequestParam String apiPath,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate endDate = to != null ? to : LocalDate.now();
        LocalDate startDate = from != null ? from : endDate.minusDays(30);

        return ResponseEntity.ok(
                statisticsService.getTotalCallCount(apiPath, startDate, endDate)
        );
    }
}