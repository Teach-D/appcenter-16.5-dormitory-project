package com.example.appcenter_project.domain.weather.controller;

import com.example.appcenter_project.domain.weather.dto.response.ResponseWeatherDto;
import com.example.appcenter_project.domain.weather.service.WeatherService;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    WeatherService weatherService;

    @MockBean
    SlackErrorNotifier slackErrorNotifier;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("200 반환 — BR-662 정상 위경도 요청 시 날씨 데이터 반환")
    void should_return_200_when_valid_lat_lon() throws Exception {
        ResponseWeatherDto response = ResponseWeatherDto.builder()
                .nx(54).ny(124).baseDate("20260705").baseTime("1400")
                .tmp("28").reh("60").wsd("2.5").vec("180").sky("1").pty("0").build();
        given(weatherService.getWeather(37.3749, 126.6368)).willReturn(response);

        ResultActions result = mockMvc.perform(get("/weather")
                .param("lat", "37.3749")
                .param("lon", "126.6368"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nx").value(54))
                .andExpect(jsonPath("$.ny").value(124));
    }

    @Test
    @DisplayName("400 반환 — BR-662 lat 파라미터 누락")
    void should_return_400_when_lat_is_missing() throws Exception {
        ResultActions result = mockMvc.perform(get("/weather")
                .param("lon", "126.6368"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — BR-662 lon 파라미터 누락")
    void should_return_400_when_lon_is_missing() throws Exception {
        ResultActions result = mockMvc.perform(get("/weather")
                .param("lat", "37.3749"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — BR-662 lat에 숫자가 아닌 값 전달 시 타입 불일치")
    void should_return_400_when_lat_is_non_numeric() throws Exception {
        ResultActions result = mockMvc.perform(get("/weather")
                .param("lat", "abc")
                .param("lon", "126.6368"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — BR-662 WEATHER_INVALID_LOCATION 예외 시 400 응답")
    void should_return_400_when_weather_service_throws_invalid_location() throws Exception {
        given(weatherService.getWeather(0.0, 0.0))
                .willThrow(new CustomException(ErrorCode.WEATHER_INVALID_LOCATION));

        ResultActions result = mockMvc.perform(get("/weather")
                .param("lat", "0.0")
                .param("lon", "0.0"));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(25002));
    }

    @Test
    @DisplayName("500 반환 — BR-662 WEATHER_API_ERROR 예외 시 500 응답")
    void should_return_500_when_weather_service_throws_api_error() throws Exception {
        given(weatherService.getWeather(37.3749, 126.6368))
                .willThrow(new CustomException(ErrorCode.WEATHER_API_ERROR));

        ResultActions result = mockMvc.perform(get("/weather")
                .param("lat", "37.3749")
                .param("lon", "126.6368"));

        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(25001));
    }
}
