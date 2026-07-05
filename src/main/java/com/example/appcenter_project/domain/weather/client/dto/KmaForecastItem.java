package com.example.appcenter_project.domain.weather.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KmaForecastItem {
    private String category;
    private String fcstDate;
    private String fcstTime;
    private String fcstValue;
    private int nx;
    private int ny;
    private String baseDate;
    private String baseTime;
}
