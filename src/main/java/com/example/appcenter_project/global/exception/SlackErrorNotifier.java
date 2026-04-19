package com.example.appcenter_project.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class SlackErrorNotifier {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${slack.webhook-url:}")
    private String slackWebhookUrl;

    public void sendErrorAlert(Exception ex, HttpServletRequest request) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank()) return;

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String payload = buildPayload(ex, request, timestamp);

        try {
            HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(slackWebhookUrl))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            log.error("Slack 에러 알림 전송 실패: {}", e.getMessage());
        }
    }

    private String buildPayload(Exception ex, HttpServletRequest request, String timestamp) {
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String exceptionName = ex.getClass().getSimpleName();
        String message = ex.getMessage() != null ? ex.getMessage().replace("\"", "'") : "null";

        return String.format(
                "{\"text\": \"[서버 에러 발생]\\n• 시각: %s\\n• 요청: %s %s\\n• 예외: %s\\n• 메시지: %s\"}",
                timestamp, method, uri, exceptionName, message
        );
    }
}
