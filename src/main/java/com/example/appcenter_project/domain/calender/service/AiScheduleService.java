package com.example.appcenter_project.domain.calender.service;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.calender.client.AiScheduleExtractClient;
import com.example.appcenter_project.domain.calender.dto.ai.AiScheduleExtractItem;
import com.example.appcenter_project.domain.calender.dto.ai.AiScheduleExtractResponse;
import com.example.appcenter_project.domain.calender.entity.Calender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiScheduleService {

    public enum ProcessResult { SUCCESS, NO_SCHEDULE, FAILED }

    private static final int TITLE_MAX_LENGTH = 100;
    private static final String DEFAULT_TITLE = "[일정]";

    private final AiScheduleExtractClient aiClient;
    private final AiCalendarPersistenceService persistenceService;

    public ProcessResult process(CrawledAnnouncement announcement) {
        Long id = announcement.getId();

        boolean titleBlank = announcement.getTitle() == null || announcement.getTitle().isBlank();
        boolean contentBlank = announcement.getContent() == null || announcement.getContent().isBlank();
        if (titleBlank && contentBlank) {
            persistenceService.markNoSchedule(id);
            return ProcessResult.NO_SCHEDULE;
        }

        String request = buildRequest(announcement);

        try {
            AiScheduleExtractResponse response = aiClient.extract(request);

            if (isEmpty(response)) {
                persistenceService.markNoSchedule(id);
                return ProcessResult.NO_SCHEDULE;
            }

            List<Calender> calendars = buildCalendars(id, announcement.getTitle(), announcement.getLink(), response);

            if (calendars.isEmpty()) {
                persistenceService.markNoSchedule(id);
                return ProcessResult.NO_SCHEDULE;
            }

            persistenceService.saveSuccess(id, calendars);
            return ProcessResult.SUCCESS;

        } catch (Exception e) {
            log.warn("공지 {} AI 일정 추출 실패: {}", id, e.getMessage());
            try {
                persistenceService.markFailed(id, e.getMessage());
            } catch (Exception persistError) {
                log.error("공지 {} 실패 상태 저장 중 추가 오류 발생", id, persistError);
            }
            return ProcessResult.FAILED;
        }
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

    private List<Calender> buildCalendars(Long id, String fallbackTitle, String link,
                                          AiScheduleExtractResponse res) {

        List<Calender> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (AiScheduleExtractItem item : res.getData()) {

            LocalDate start = parse(item.getStartDate());
            if (start == null) continue;

            LocalDate end = Optional.ofNullable(parse(item.getEndDate())).orElse(start);

            String rawTitle = (item.getTitle() != null && !item.getTitle().isBlank())
                    ? item.getTitle()
                    : fallbackTitle;
            if (rawTitle == null || rawTitle.isBlank()) {
                rawTitle = DEFAULT_TITLE;
            }
            String title = rawTitle.length() > TITLE_MAX_LENGTH
                    ? rawTitle.substring(0, TITLE_MAX_LENGTH)
                    : rawTitle;

            String key = title + "|" + start + "|" + end;
            if (!seen.add(key)) continue;

            result.add(Calender.ofAiGenerated(start, end, title, link, id));
        }

        return result;
    }

    private LocalDate parse(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            log.warn("AI 응답에 잘못된 날짜 형식: '{}'", date);
            return null;
        }
    }
}