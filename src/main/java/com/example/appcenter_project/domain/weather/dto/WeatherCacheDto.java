package com.example.appcenter_project.domain.weather.dto;

import com.example.appcenter_project.domain.weather.dto.response.ResponseWeatherDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherCacheDto {
    private int nx;
    private int ny;
    private String baseDate;
    private String baseTime;
    private String tmp;
    private String reh;
    private String wsd;
    private String vec;
    private String sky;
    private String pty;
    private Instant cachedAt;

    public ResponseWeatherDto toResponse() {
        return ResponseWeatherDto.builder()
                .nx(nx)
                .ny(ny)
                .baseDate(baseDate)
                .baseTime(baseTime)
                .tmp(tmp)
                .reh(reh)
                .wsd(wsd)
                .vec(vec)
                .sky(sky)
                .pty(pty)
                .build();
    }
}
