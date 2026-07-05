package com.example.appcenter_project.domain.weather.service;

import com.example.appcenter_project.domain.weather.client.KmaForecastClient;
import com.example.appcenter_project.domain.weather.dto.WeatherCacheDto;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.scheduler.WeatherPrefetchScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WeatherPrefetchSchedulerTest {

    @Mock
    KmaForecastClient kmaForecastClient;

    @Mock
    WeatherCacheService weatherCacheService;

    @InjectMocks
    WeatherPrefetchScheduler weatherPrefetchScheduler;

    private WeatherCacheDto buildCacheDto() {
        return new WeatherCacheDto(54, 124, "20260705", "1400", "28", "60", "2.5", "180", "1", "0", Instant.now());
    }

    @Test
    @DisplayName("인천대 격자 API 호출 — BR-662 스케줄러 실행 시 nx=54, ny=124로 기상청 API 1회 호출")
    void should_call_kma_api_with_inu_grid_when_prefetch() {
        WeatherCacheDto dto = buildCacheDto();
        given(kmaForecastClient.fetch(54, 124)).willReturn(dto);

        weatherPrefetchScheduler.prefetch();

        verify(kmaForecastClient).fetch(54, 124);
    }

    @Test
    @DisplayName("Redis 저장 — BR-662 스케줄러 실행 시 weather:54:124 키에 데이터 저장")
    void should_put_to_cache_with_inu_grid_when_prefetch() {
        WeatherCacheDto dto = buildCacheDto();
        given(kmaForecastClient.fetch(54, 124)).willReturn(dto);

        weatherPrefetchScheduler.prefetch();

        verify(weatherCacheService).put(54, 124, dto);
    }

    @Test
    @DisplayName("예외 무시 정상 종료 — BR-662 스케줄러 중 기상청 API 오류 발생 시 예외 없이 정상 종료")
    void should_complete_without_exception_when_kma_api_fails_during_prefetch() {
        given(kmaForecastClient.fetch(54, 124))
                .willThrow(new CustomException(ErrorCode.WEATHER_API_ERROR));

        assertThatCode(() -> weatherPrefetchScheduler.prefetch())
                .doesNotThrowAnyException();
    }
}
