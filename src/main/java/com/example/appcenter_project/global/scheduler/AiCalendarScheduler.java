package com.example.appcenter_project.global.scheduler;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.ScheduleExtractStatus;
import com.example.appcenter_project.domain.announcement.repository.CrawledAnnouncementRepository;
import com.example.appcenter_project.domain.calender.client.AiScheduleExtractClient;
import com.example.appcenter_project.domain.calender.service.AiScheduleService;
import com.example.appcenter_project.domain.calender.service.AiScheduleService.ProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.example.appcenter_project.domain.announcement.enums.ScheduleExtractStatus.SUCCESS;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiCalendarScheduler {

    private static final int BATCH_SIZE = 50;
    private static final int CRAWLED_DATE_CUTOFF_DAYS = 3;
    private static final long REQUEST_DELAY_MS = 1_000L;
    private static final List<ScheduleExtractStatus> TARGET_STATUSES =
            List.of(ScheduleExtractStatus.PENDING, ScheduleExtractStatus.FAILED);

    private final CrawledAnnouncementRepository crawledAnnouncementRepository;
    private final AiScheduleService aiScheduleService;
    private final AiScheduleExtractClient aiClient;

    //매일 새벽 05:00(Asia/Seoul)에 PENDING/FAILED 상태의 공지를 일괄 처리.
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void extractSchedules() {
        if (!aiClient.isConfigured()) {
            log.error("AI 클라이언트 미설정 (ai-url 또는 api key 누락) — AI 일정 추출 스킵");
            return;
        }

        LocalDate cutoff = LocalDate.now().minusDays(CRAWLED_DATE_CUTOFF_DAYS);
        log.info("AI 캘린더 일정 추출 시작 (cutoff={})", cutoff);

        long lastId = 0L;
        int processed = 0;
        int succeeded = 0;
        int noSchedule = 0;
        int failed = 0;

        while (true) {
            List<CrawledAnnouncement> batch = crawledAnnouncementRepository.findScheduleExtractTargets(
                    TARGET_STATUSES, cutoff, lastId, PageRequest.of(0, BATCH_SIZE));

            if (batch.isEmpty()) break;

            for (CrawledAnnouncement announcement : batch) {
                Long id = announcement.getId();
                lastId = id;
                try {
                    ProcessResult result = aiScheduleService.process(announcement);
                    switch (result) {
                        case SUCCESS -> succeeded++;
                        case NO_SCHEDULE -> noSchedule++;
                        case FAILED -> failed++;
                    }
                } catch (Exception e) {
                    log.error("공지 처리 중 예기치 못한 예외 - ID: {}", id, e);
                    failed++;
                }
                processed++;
                sleep(REQUEST_DELAY_MS);
            }
        }

        log.info("AI 캘린더 일정 추출 종료 - 처리:{}, 성공:{}, 일정없음:{}, 실패:{}",
                processed, succeeded, noSchedule, failed);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}