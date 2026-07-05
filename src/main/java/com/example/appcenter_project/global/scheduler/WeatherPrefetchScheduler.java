package com.example.appcenter_project.global.scheduler;

import com.example.appcenter_project.domain.weather.client.KmaForecastClient;
import com.example.appcenter_project.domain.weather.dto.WeatherCacheDto;
import com.example.appcenter_project.domain.weather.service.WeatherCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherPrefetchScheduler {

    private final KmaForecastClient kmaForecastClient;
    private final WeatherCacheService weatherCacheService;

    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *")
    public void prefetch() {
        try {
            WeatherCacheDto dto = kmaForecastClient.fetch(54, 124);
            weatherCacheService.put(54, 124, dto);
        } catch (Exception e) {
            log.error("[WeatherPrefetchScheduler] 기상청 API 프리페치 실패: {}", e.getMessage(), e);
        }
    }
}
