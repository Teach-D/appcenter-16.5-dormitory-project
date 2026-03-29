package com.example.appcenter_project.domain.fcm.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseFcmStatsDto {
    private String date;
    private long successCount;
    private long failCount;
}
