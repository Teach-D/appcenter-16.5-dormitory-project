package com.example.appcenter_project.domain.weather.service;

import com.example.appcenter_project.domain.weather.client.KmaForecastClient;
import com.example.appcenter_project.domain.weather.dto.WeatherCacheDto;
import com.example.appcenter_project.domain.weather.dto.response.ResponseWeatherDto;
import com.example.appcenter_project.domain.weather.util.GridConverter;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherCacheService weatherCacheService;
    private final KmaForecastClient kmaForecastClient;

    public ResponseWeatherDto getWeather(double lat, double lon) {
        if (lat < 33.0 || lat > 38.9 || lon < 124.0 || lon > 132.0) {
            throw new CustomException(ErrorCode.WEATHER_INVALID_LOCATION);
        }

        int[] grid = GridConverter.toGrid(lat, lon);
        int nx = grid[0];
        int ny = grid[1];

        Optional<WeatherCacheDto> cached = weatherCacheService.get(nx, ny);
        if (cached.isPresent()) {
            return cached.get().toResponse();
        }

        WeatherCacheDto fetched = kmaForecastClient.fetch(nx, ny);
        weatherCacheService.put(nx, ny, fetched);
        return fetched.toResponse();
    }
}
