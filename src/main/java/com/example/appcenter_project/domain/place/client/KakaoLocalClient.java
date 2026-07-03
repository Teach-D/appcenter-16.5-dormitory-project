package com.example.appcenter_project.domain.place.client;

import com.example.appcenter_project.domain.place.client.dto.KakaoPlaceDocument;
import com.example.appcenter_project.domain.place.client.dto.KakaoPlaceSearchResponse;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class KakaoLocalClient {

    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 500L;

    @Value("${kakao.local.base-url:https://dapi.kakao.com}")
    private String baseUrl;

    @Value("${kakao.local.rest-api-key:}")
    private String restApiKey;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(READ_TIMEOUT);
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public Optional<KakaoPlaceDocument> searchFirst(String keyword) {
        List<KakaoPlaceDocument> results = searchTop(keyword, 1);
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    public List<KakaoPlaceDocument> searchTop(String keyword, int size) {
        if (size < 1) {
            size = 1;
        }
        if (size > 15) {
            size = 15;
        }

        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                KakaoPlaceSearchResponse response = restClient.get()
                        .uri(baseUrl + "/v2/local/search/keyword.json?query={keyword}&size={size}",
                                keyword, size)
                        .header("Authorization", "KakaoAK " + restApiKey)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
                        })
                        .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
                        })
                        .body(KakaoPlaceSearchResponse.class);

                if (response == null || response.getDocuments() == null) {
                    return List.of();
                }
                return response.getDocuments();
            } catch (CustomException e) {
                lastError = e;
                log.warn("카카오 로컬 API 호출 실패 (시도 {}/{}): {}", attempt, MAX_ATTEMPTS, e.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    sleep(RETRY_BACKOFF_MS * attempt);
                }
            }
        }
        throw lastError != null ? lastError : new CustomException(ErrorCode.KAKAO_API_ERROR);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}
