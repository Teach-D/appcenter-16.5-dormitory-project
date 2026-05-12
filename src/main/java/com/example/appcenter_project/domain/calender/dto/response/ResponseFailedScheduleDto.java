package com.example.appcenter_project.domain.calender.dto.response;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseFailedScheduleDto {

    private Long id;
    private String title;
    private LocalDate crawledDate;
    private int retryCount;
    private String lastError;
    private LocalDateTime lastAttemptAt;

    public static ResponseFailedScheduleDto from(CrawledAnnouncement ca) {
        return new ResponseFailedScheduleDto(
                ca.getId(),
                ca.getTitle(),
                ca.getCrawledDate(),
                ca.getScheduleExtractRetryCount(),
                ca.getScheduleExtractLastError(),
                ca.getScheduleExtractedAt()
        );
    }
}