package com.example.appcenter_project.domain.weather.service;

import com.example.appcenter_project.domain.weather.client.KmaForecastClient;
import com.example.appcenter_project.domain.weather.dto.WeatherCacheDto;
import com.example.appcenter_project.domain.weather.dto.response.ResponseWeatherDto;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    WeatherCacheService weatherCacheService;

    @Mock
    KmaForecastClient kmaForecastClient;

    @InjectMocks
    WeatherService weatherService;

    private WeatherCacheDto buildCacheDto() {
        return new WeatherCacheDto(54, 124, "20260705", "1400", "28", "60", "2.5", "180", "1", "0", Instant.now());
    }

    @Test
    @DisplayName("캐시 HIT — BR-662 Redis 키 존재 시 기상청 API 호출 없이 캐시 데이터 반환")
    void should_return_cached_data_without_api_call_when_cache_hit() {
        WeatherCacheDto cached = buildCacheDto();
        given(weatherCacheService.get(54, 124)).willReturn(Optional.of(cached));

        ResponseWeatherDto result = weatherService.getWeather(37.3749, 126.6368);

        assertThat(result).isNotNull();
        verify(kmaForecastClient, never()).fetch(anyInt(), anyInt());
    }

    @Test
    @DisplayName("캐시 HIT 응답 nx — BR-662 캐시 데이터의 nx가 응답에 포함됨")
    void should_return_nx_from_cache_when_cache_hit() {
        WeatherCacheDto cached = buildCacheDto();
        given(weatherCacheService.get(54, 124)).willReturn(Optional.of(cached));

        ResponseWeatherDto result = weatherService.getWeather(37.3749, 126.6368);

        assertThat(result.getNx()).isEqualTo(54);
    }

    @Test
    @DisplayName("캐시 MISS API 호출 — BR-662 Redis 키 없을 때 기상청 API 1회 호출")
    void should_call_kma_api_once_when_cache_miss() {
        WeatherCacheDto fetched = buildCacheDto();
        given(weatherCacheService.get(54, 124)).willReturn(Optional.empty());
        given(kmaForecastClient.fetch(54, 124)).willReturn(fetched);

        weatherService.getWeather(37.3749, 126.6368);

        verify(kmaForecastClient).fetch(54, 124);
    }

    @Test
    @DisplayName("캐시 MISS Redis 저장 — BR-662 API 호출 후 Redis에 결과 저장")
    void should_put_to_cache_after_api_call_when_cache_miss() {
        WeatherCacheDto fetched = buildCacheDto();
        given(weatherCacheService.get(54, 124)).willReturn(Optional.empty());
        given(kmaForecastClient.fetch(54, 124)).willReturn(fetched);

        weatherService.getWeather(37.3749, 126.6368);

        verify(weatherCacheService).put(54, 124, fetched);
    }

    @Test
    @DisplayName("캐시 MISS 응답 반환 — BR-662 API 결과가 응답 DTO로 변환되어 반환됨")
    void should_return_response_dto_when_cache_miss() {
        WeatherCacheDto fetched = buildCacheDto();
        given(weatherCacheService.get(54, 124)).willReturn(Optional.empty());
        given(kmaForecastClient.fetch(54, 124)).willReturn(fetched);

        ResponseWeatherDto result = weatherService.getWeather(37.3749, 126.6368);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("WEATHER_INVALID_LOCATION 예외 — BR-662 위도가 대한민국 범위(33.0~38.9) 미만")
    void should_throw_CustomException_when_lat_below_range() {
        double lat = 32.9;
        double lon = 126.6368;

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WEATHER_INVALID_LOCATION);
    }

    @Test
    @DisplayName("WEATHER_INVALID_LOCATION 예외 — BR-662 위도가 대한민국 범위(33.0~38.9) 초과")
    void should_throw_CustomException_when_lat_above_range() {
        double lat = 39.0;
        double lon = 126.6368;

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WEATHER_INVALID_LOCATION);
    }

    @Test
    @DisplayName("WEATHER_INVALID_LOCATION 예외 — BR-662 경도가 대한민국 범위(124.0~132.0) 미만")
    void should_throw_CustomException_when_lon_below_range() {
        double lat = 37.3749;
        double lon = 123.9;

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WEATHER_INVALID_LOCATION);
    }

    @Test
    @DisplayName("WEATHER_INVALID_LOCATION 예외 — BR-662 경도가 대한민국 범위(124.0~132.0) 초과")
    void should_throw_CustomException_when_lon_above_range() {
        double lat = 37.3749;
        double lon = 132.1;

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WEATHER_INVALID_LOCATION);
    }

    @Test
    @DisplayName("WEATHER_INVALID_LOCATION 예외 — BR-662 위경도 모두 0.0 (범위 외)")
    void should_throw_CustomException_when_lat_lon_are_zero() {
        double lat = 0.0;
        double lon = 0.0;

        assertThatThrownBy(() -> weatherService.getWeather(lat, lon))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WEATHER_INVALID_LOCATION);
    }

    @Test
    @DisplayName("WEATHER_API_ERROR 예외 전파 — BR-662 기상청 API 오류 시 예외가 그대로 전파됨")
    void should_propagate_CustomException_when_kma_api_fails() {
        given(weatherCacheService.get(54, 124)).willReturn(Optional.empty());
        given(kmaForecastClient.fetch(54, 124))
                .willThrow(new CustomException(ErrorCode.WEATHER_API_ERROR));

        assertThatThrownBy(() -> weatherService.getWeather(37.3749, 126.6368))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WEATHER_API_ERROR);
    }
}
