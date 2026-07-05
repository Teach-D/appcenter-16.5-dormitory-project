package com.example.appcenter_project.domain.weather.client;

import com.example.appcenter_project.domain.weather.client.dto.KmaForecastItem;
import com.example.appcenter_project.domain.weather.client.dto.KmaForecastResponse;
import com.example.appcenter_project.domain.weather.dto.WeatherCacheDto;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KmaForecastClient {

    private static final String BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private static final int[] BASE_HOURS = {2, 5, 8, 11, 14, 17, 20, 23};

    @Value("${weather.api.service-key}")
    private String serviceKey;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(10000);
        restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public WeatherCacheDto fetch(int nx, int ny) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        int baseHour = resolveBaseHour(now.getHour());
        LocalDateTime baseDateTime = now.withHour(baseHour).withMinute(0).withSecond(0).withNano(0);
        if (baseHour > now.getHour()) {
            baseDateTime = baseDateTime.minusDays(1);
        }

        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = String.format("%02d00", baseHour);

        KmaForecastResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> java.net.URI.create(
                            BASE_URL + "?serviceKey=" + serviceKey
                                    + "&numOfRows=1000&pageNo=1&dataType=JSON"
                                    + "&base_date=" + baseDate
                                    + "&base_time=" + baseTime
                                    + "&nx=" + nx + "&ny=" + ny))
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        throw new CustomException(ErrorCode.WEATHER_API_ERROR);
                    })
                    .body(KmaForecastResponse.class);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.WEATHER_API_ERROR);
        }

        if (response == null || response.getResponse() == null
                || response.getResponse().getHeader() == null
                || !"00".equals(response.getResponse().getHeader().getResultCode())) {
            throw new CustomException(ErrorCode.WEATHER_API_ERROR);
        }

        List<KmaForecastItem> items = response.getResponse().getBody().getItems().getItem();

        String fcstTime = items.stream()
                .map(KmaForecastItem::getFcstTime)
                .findFirst()
                .orElse(baseTime);

        Map<String, String> categoryMap = items.stream()
                .filter(item -> item.getFcstTime().equals(fcstTime))
                .collect(Collectors.toMap(KmaForecastItem::getCategory, KmaForecastItem::getFcstValue, (a, b) -> a));

        return new WeatherCacheDto(
                nx, ny, baseDate, baseTime,
                categoryMap.get("TMP"),
                categoryMap.get("REH"),
                categoryMap.get("WSD"),
                categoryMap.get("VEC"),
                categoryMap.get("SKY"),
                categoryMap.get("PTY"),
                Instant.now()
        );
    }

    private int resolveBaseHour(int currentHour) {
        int result = BASE_HOURS[0];
        for (int h : BASE_HOURS) {
            if (currentHour >= h) {
                result = h;
            }
        }
        return result;
    }
}
