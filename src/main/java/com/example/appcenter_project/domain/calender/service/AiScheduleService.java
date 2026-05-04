package com.example.appcenter_project.domain.calender.service;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.ScheduleExtractStatus;
import com.example.appcenter_project.domain.calender.client.AiScheduleExtractClient;
import com.example.appcenter_project.domain.calender.dto.ai.AiScheduleExtractResponse;
import com.example.appcenter_project.domain.calender.entity.Calender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiScheduleService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalTime START = LocalTime.of(3, 0);
    private static final LocalTime END = LocalTime.of(7, 0);

    private final AiScheduleExtractClient aiClient;
    private final AiCalendarPersistenceService persistenceService;

    public boolean process(CrawledAnnouncement announcement) {
        Long id = announcement.getId();

        String request = buildRequest(announcement);
        if (request.isBlank()) {
            persistenceService.markNoSchedule(id);
            return false;
        }

        try {
            var response = aiClient.extract(request);

            if (isEmpty(response)) {
                persistenceService.markNoSchedule(id);
                return false;
            }

            var calendars = buildCalendars(id, announcement.getTitle(), response);

            if (calendars.isEmpty()) {
                persistenceService.markNoSchedule(id);
                return false;
            }

            persistenceService.saveSuccess(id, calendars);
            return false;

        } catch (Exception e) {
            log.warn("공지 {} 실패: {}", id, e.getMessage());
            persistenceService.markFailed(id, e.getMessage());
            return true;
        }
    }

    public boolean isRunnable() {
        return isWithinWindow() && aiClient.isConfigured();
    }

    public boolean isWithinWindow() {
        LocalTime now = ZonedDateTime.now(ZONE).toLocalTime();
        return !now.isBefore(START) && now.isBefore(END);
    }

    public List<ScheduleExtractStatus> getTargetStatuses() {
        return List.of(ScheduleExtractStatus.PENDING, ScheduleExtractStatus.FAILED);
    }

    private boolean isEmpty(AiScheduleExtractResponse res) {
        return res == null || res.getData() == null || res.getData().isEmpty();
    }

    private String buildRequest(CrawledAnnouncement a) {
        return "[제목]\n" + nullSafe(a.getTitle())
                + "\n\n[내용]\n" + nullSafe(a.getContent());
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private List<Calender> buildCalendars(Long id, String fallbackTitle,
                                          AiScheduleExtractResponse res) {

        List<Calender> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (var item : res.getData()) {

            LocalDate start = parse(item.getStartDate());
            if (start == null) continue;

            LocalDate end = Optional.ofNullable(parse(item.getEndDate()))
                    .orElse(start);

            String title = (item.getTitle() != null && !item.getTitle().isBlank())
                    ? item.getTitle()
                    : fallbackTitle;

            if (title.length() > 100) {
                title = title.substring(0, 100);
            }

            String key = title + "|" + start;
            if (!seen.add(key)) continue;

            result.add(Calender.ofAiGenerated(start, end, title, null, id));
        }

        return result;
    }

    private LocalDate parse(String date) {
        try {
            return (date == null || date.isBlank()) ? null : LocalDate.parse(date);
        } catch (Exception e) {
            return null;
        }
    }
}