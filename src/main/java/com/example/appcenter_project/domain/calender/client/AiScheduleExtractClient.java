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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;

@Slf4j
@Component
public class AiScheduleExtractClient {

    private static final Duration READ_TIMEOUT = Duration.ofSeconds(630);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 2_000L;

    @Value("${app.urls.ai-url}")
    private String baseUrl;

    @Value("${shared.api.key:}")
    private String apiKey;

    private RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(READ_TIMEOUT);
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank()
                && apiKey != null && !apiKey.isBlank();
    }

    public AiScheduleExtractResponse extract(Long announcementId, String textBody) {
        log.info("[AI-EXTRACT][{}] 요청 전송 — 본문 {}자", announcementId, textBody.length());

        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                long startMs = System.currentTimeMillis();

                String rawJson = restClient.post()
                        .uri(baseUrl + "/shared/extract-schedule")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .contentType(MediaType.TEXT_PLAIN)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(textBody)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, (req, res) -> {
                            throw new IllegalStateException("AI 서버 오류: HTTP " + res.getStatusCode());
                        })
                        .body(String.class);

                long elapsed = System.currentTimeMillis() - startMs;
                log.info("[AI-EXTRACT][{}] 응답 수신 — {}ms, raw={}",
                        announcementId, elapsed, abbreviate(rawJson, 2000));

                return objectMapper.readValue(rawJson, AiScheduleExtractResponse.class);

            } catch (ResourceAccessException e) {
                throw e;
            } catch (Exception e) {
                lastError = e instanceof RuntimeException re
                        ? re
                        : new IllegalStateException("AI 응답 파싱 실패: " + e.getMessage(), e);
                log.warn("[AI-EXTRACT][{}] 실패 (시도 {}/{}): {}",
                        announcementId, attempt, MAX_ATTEMPTS, e.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    sleep(RETRY_BACKOFF_MS * attempt);
                }
            }
        }
        throw lastError;
    }

    private String abbreviate(String s, int max) {
        if (s == null) return "null";
        return s.length() <= max ? s : s.substring(0, max) + "...(총 " + s.length() + "자)";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 추출 재시도 대기 중 인터럽트", e);
        }
    }
}