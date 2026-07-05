package com.example.appcenter_project.domain.weather.controller;

import com.example.appcenter_project.domain.weather.dto.response.ResponseWeatherDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "날씨", description = "기상청 단기예보 Redis 캐싱 API")
public interface WeatherApiSpecification {

    @Operation(summary = "날씨 조회", description = "위경도를 기상청 격자 좌표로 변환하고 단기예보 1건을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 위경도"),
            @ApiResponse(responseCode = "500", description = "기상청 API 오류")
    })
    ResponseEntity<ResponseWeatherDto> getWeather(double lat, double lon);
}
