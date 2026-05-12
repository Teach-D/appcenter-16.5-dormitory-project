package com.example.appcenter_project.domain.calender.client;

import com.example.appcenter_project.domain.calender.dto.ai.AiScheduleExtractResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class AiScheduleExtractClient {

    @Value("${app.urls.ai-url}")
    private String baseUrl;

    @Value("${shared.api.key:}")
    private String apiKey;

    @Value("${ai.schedule.timeout-seconds:600}")
    private long timeoutSeconds;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setConnectTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank()
                && apiKey != null && !apiKey.isBlank();
    }

    public AiScheduleExtractResponse extract(String textBody) {
        log.debug("AI 서버 일정 추출 요청 (본문 길이: {}자)", textBody.length());
        return restClient.post()
                .uri(baseUrl + "/shared/extract-schedule")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.TEXT_PLAIN)
                .accept(MediaType.APPLICATION_JSON)
                .body(textBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new IllegalStateException("AI 서버 오류: HTTP " + res.getStatusCode());
                })
                .body(AiScheduleExtractResponse.class);
    }
}