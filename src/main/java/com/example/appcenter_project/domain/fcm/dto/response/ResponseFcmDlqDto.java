package com.example.appcenter_project.domain.fcm.dto.response;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.OutboxStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseFcmDlqDto {

    private Long id;
    private String token;
    private String title;
    private String body;
    private OutboxStatus status;
    private int retryCount;
    private int maxRetry;
    private String lastErrorCode;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;

    public static ResponseFcmDlqDto from(FcmOutbox outbox) {
        return ResponseFcmDlqDto.builder()
                .id(outbox.getId())
                .token(outbox.getToken().substring(0, Math.min(20, outbox.getToken().length())) + "...")
                .title(outbox.getTitle())
                .body(outbox.getBody())
                .status(outbox.getStatus())
                .retryCount(outbox.getRetryCount())
                .maxRetry(outbox.getMaxRetry())
                .lastErrorCode(outbox.getLastErrorCode())
                .nextRetryAt(outbox.getNextRetryAt())
                .createdAt(outbox.getCreatedDate())
                .build();
    }
}
