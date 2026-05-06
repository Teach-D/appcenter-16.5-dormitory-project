package com.example.appcenter_project.domain.calender.service;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.ScheduleExtractStatus;
import com.example.appcenter_project.domain.announcement.repository.CrawledAnnouncementRepository;
import com.example.appcenter_project.domain.calender.entity.Calender;
import com.example.appcenter_project.domain.calender.repository.CalenderRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCalendarPersistenceService {

    private final CalenderRepository calenderRepository;
    private final CrawledAnnouncementRepository crawledAnnouncementRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSuccess(Long announcementId, List<Calender> calenders) {
        CrawledAnnouncement announcement = crawledAnnouncementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        // 첫 PENDING 처리에는 기존 AI 일정이 있을 수 없으므로 DELETE 스킵 (재시도일 때만 정리)
        if (announcement.getScheduleExtractStatus() != ScheduleExtractStatus.PENDING) {
            calenderRepository.deleteAiGeneratedBySourceId(announcementId);
        }
        calenderRepository.saveAll(calenders);
        announcement.markSuccess();
        log.info("AI 일정 추출 성공 - 공지 ID: {}, 저장된 일정 수: {}", announcementId, calenders.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markNoSchedule(Long announcementId) {
        CrawledAnnouncement announcement = crawledAnnouncementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        if (announcement.getScheduleExtractStatus() != ScheduleExtractStatus.PENDING) {
            calenderRepository.deleteAiGeneratedBySourceId(announcementId);
        }
        announcement.markNoSchedule();
        log.debug("AI 일정 없음 처리 - 공지 ID: {}", announcementId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long announcementId, String reason) {
        CrawledAnnouncement announcement = crawledAnnouncementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        announcement.markFailed(reason);
        log.warn("AI 일정 추출 실패 - 공지 ID: {}, 사유: {}, 재시도 횟수: {}",
                announcementId, reason, announcement.getScheduleExtractRetryCount());
    }
}