package com.example.appcenter_project.domain.weather.controller;

import com.example.appcenter_project.domain.weather.dto.response.ResponseWeatherDto;
import com.example.appcenter_project.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController implements WeatherApiSpecification {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<ResponseWeatherDto> getWeather(
            @RequestParam double lat,
            @RequestParam double lon) {
        return ResponseEntity.ok(weatherService.getWeather(lat, lon));
    }
}
