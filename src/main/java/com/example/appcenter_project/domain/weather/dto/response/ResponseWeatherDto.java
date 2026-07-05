package com.example.appcenter_project.domain.weather.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResponseWeatherDto {
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
}
