package com.example.appcenter_project.common.metrics;

import com.example.appcenter_project.common.metrics.entity.ApiCallStatistics;
import com.example.appcenter_project.common.metrics.repository.ApiCallStatisticsRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 영구 저장되는 메트릭 서비스
 *
 * API 호출 통계를 데이터베이스에 저장하고 Prometheus에 노출합니다.
 * 애플리케이션이 재시작되어도 누적 통계가 유지됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentMetricsService {

    private final ApiCallStatisticsRepository repository;
    private final MeterRegistry meterRegistry;

    // 메모리 캐시: API 이름 -> 현재 호출 횟수
    private final ConcurrentHashMap<String, AtomicLong> metricsCache = new ConcurrentHashMap<>();

    /**
     * 애플리케이션 시작 시 DB에서 기존 통계를 로드하고 Gauge를 등록합니다.
     */
    @PostConstruct
    public void initializeMetrics() {
        log.info("Initializing persistent metrics from database...");

        List<ApiCallStatistics> allStats = repository.findAll();

        for (ApiCallStatistics stat : allStats) {
            AtomicLong counter = new AtomicLong(stat.getTotalCalls());
            metricsCache.put(stat.getApiName(), counter);

            // Gauge로 등록 (Prometheus가 읽을 수 있도록)
            // 메트릭 이름의 점(.)을 언더스코어(_)로 변환
            String metricName = stat.getApiName().replace('.', '_') + "_persistent_total";
            Gauge.builder(metricName, counter, AtomicLong::get)
                    .description("Persistent total calls for " + stat.getApiName())
                    .tag("persistent", "true")
                    .register(meterRegistry);

            log.info("Loaded metric: {} = {} calls", stat.getApiName(), stat.getTotalCalls());
        }

        log.info("Loaded {} persistent metrics from database", allStats.size());
    }

    /**
     * API 호출 횟수를 1 증가시키고 DB에 저장합니다.
     *
     * @param apiName API 이름 (예: "survey.find", "announcement.find")
     */
    @Transactional
    public void incrementApiCall(String apiName) {
        // 메모리 캐시 업데이트
        AtomicLong counter = metricsCache.computeIfAbsent(apiName, key -> {
            // 캐시에 없으면 새로 생성하고 Gauge 등록
            // 메트릭 이름의 점(.)을 언더스코어(_)로 변환
            AtomicLong newCounter = new AtomicLong(0);
            String metricName = apiName.replace('.', '_') + "_persistent_total";
            Gauge.builder(metricName, newCounter, AtomicLong::get)
                    .description("Persistent total calls for " + apiName)
                    .tag("persistent", "true")
                    .register(meterRegistry);
            return newCounter;
        });

        counter.incrementAndGet();

        // DB 업데이트
        ApiCallStatistics stats = repository.findByApiName(apiName)
                .orElseGet(() -> ApiCallStatistics.builder()
                        .apiName(apiName)
                        .totalCalls(0L)
                        .build());

        stats.incrementCalls();
        repository.save(stats);

        log.debug("Incremented {} to {} (persistent)", apiName, counter.get());
    }

    /**
     * 특정 API의 총 호출 횟수를 조회합니다.
     *
     * @param apiName API 이름
     * @return 총 호출 횟수
     */
    @Transactional(readOnly = true)
    public long getTotalCalls(String apiName) {
        AtomicLong counter = metricsCache.get(apiName);
        if (counter != null) {
            return counter.get();
        }

        return repository.findTotalCallsByApiName(apiName)
                .orElse(0L);
    }

    /**
     * 모든 API 통계를 조회합니다.
     *
     * @return API 통계 리스트
     */
    @Transactional(readOnly = true)
    public List<ApiCallStatistics> getAllStatistics() {
        return repository.findAll();
    }
}