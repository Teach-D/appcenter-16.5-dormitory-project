package com.example.appcenter_project.common.metrics.dto;

import com.example.appcenter_project.common.metrics.entity.ApiCallStatistics;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ApiStatisticsResponse {

    private LocalDate date;
    private String apiPath;
    private String httpMethod;
    private Long callCount;
    private LocalDateTime lastCalledAt;

    public static ApiStatisticsResponse from(ApiCallStatistics entity) {
        return ApiStatisticsResponse.builder()
                .date(entity.getCallDate())
                .apiPath(entity.getApiPath())
                .httpMethod(entity.getHttpMethod())
                .callCount(entity.getCallCount())
                .lastCalledAt(entity.getLastCalledAt())
                .build();
    }
}