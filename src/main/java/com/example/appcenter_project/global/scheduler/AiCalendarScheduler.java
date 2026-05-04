package com.example.appcenter_project.global.scheduler;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.repository.CrawledAnnouncementRepository;
import com.example.appcenter_project.domain.calender.service.AiScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiCalendarScheduler {

    private static final int BATCH_SIZE = 20;
    private static final long REQUEST_DELAY_MS = 2_000L;
    private static final long BATCH_DELAY_MS = 15_000L;
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    private final CrawledAnnouncementRepository repository;
    private final AiScheduleService aiScheduleService;

    @Scheduled(fixedDelay = 60_000)
    public void extractSchedules() {
        if (!aiScheduleService.isRunnable()) return;

        log.info("AI 캘린더 일정 추출 시작");

        int consecutiveFailures = 0;

        while (aiScheduleService.isWithinWindow()) {

            List<CrawledAnnouncement> batch =
                    repository.findScheduleExtractTargets(
                            aiScheduleService.getTargetStatuses(),
                            PageRequest.of(0, BATCH_SIZE)
                    );

            if (batch.isEmpty()) {
                log.info("처리할 공지사항 없음 → 종료");
                return;
            }

            for (CrawledAnnouncement announcement : batch) {

                if (!aiScheduleService.isWithinWindow()) return;

                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    log.error("연속 실패 {}회 → 중단", MAX_CONSECUTIVE_FAILURES);
                    return;
                }

                boolean failed = aiScheduleService.process(announcement);
                consecutiveFailures = failed ? consecutiveFailures + 1 : 0;

                sleep(REQUEST_DELAY_MS);
            }

            sleep(BATCH_DELAY_MS);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}