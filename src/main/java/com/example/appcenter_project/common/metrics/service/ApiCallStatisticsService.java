package com.example.appcenter_project.common.metrics.service;

import com.example.appcenter_project.common.metrics.dto.ApiStatisticsResponse;
import com.example.appcenter_project.common.metrics.entity.ApiCallStatistics;
import com.example.appcenter_project.common.metrics.repository.ApiCallStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallStatisticsService {

    private final ApiCallStatisticsRepository repository;

    @Async("asyncExecutor")
    @Transactional
    public void recordApiCall(String apiPath, String httpMethod) {
        try {
            LocalDate today = LocalDate.now();

            repository.findByCallDateAndApiPathAndHttpMethod(today, apiPath, httpMethod)
                    .ifPresentOrElse(
                            ApiCallStatistics::incrementCallCount,
                            () -> repository.save(
                                    ApiCallStatistics.builder()
                                            .callDate(today)
                                            .apiPath(apiPath)
                                            .httpMethod(httpMethod)
                                            .build()
                            )
                    );
        } catch (Exception e) {
            log.error("API 호출 통계 기록 실패: {} {}", httpMethod, apiPath, e);
        }
    }

    @Transactional(readOnly = true)
    public List<ApiStatisticsResponse> getStatisticsByDate(LocalDate date) {
        return repository.findByCallDate(date).stream()
                .map(ApiStatisticsResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApiStatisticsResponse> getStatisticsByDateRange(LocalDate from, LocalDate to) {
        return repository.findAllByDateRangeOrderByCallCountDesc(from, to).stream()
                .map(ApiStatisticsResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApiStatisticsResponse> getStatisticsByApiPath(String apiPath, LocalDate from, LocalDate to) {
        return repository.findByApiPathAndCallDateBetween(apiPath, from, to).stream()
                .map(ApiStatisticsResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getTotalCallCount(String apiPath, LocalDate from, LocalDate to) {
        Long count = repository.sumCallCountByApiPathAndDateRange(apiPath, from, to);
        return count != null ? count : 0L;
    }
}