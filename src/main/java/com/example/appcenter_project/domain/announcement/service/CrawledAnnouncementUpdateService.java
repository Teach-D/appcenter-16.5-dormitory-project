package com.example.appcenter_project.domain.announcement.service;

import com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile;
import com.example.appcenter_project.common.file.repository.CrawledAnnouncementFileRepository;
import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.announcement.repository.CrawledAnnouncementRepository;
import com.example.appcenter_project.domain.calender.repository.CalenderRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrawledAnnouncementUpdateService {

    private final CrawledAnnouncementRepository crawledAnnouncementRepository;
    private final CrawledAnnouncementFileRepository crawledAnnouncementFileRepository;
    private final CalenderRepository calenderRepository;

    @Transactional
    public void applyCrawlUpdate(Long id, AnnouncementCategory category, String title, String content,
                                 String writer, LocalDate crawledDate, int viewCount,
                                 List<CrawledAnnouncementFile> newFiles) {
        CrawledAnnouncement existing = crawledAnnouncementRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        // 전체 필드 갱신 + 일정 재추출 대상으로 리셋
        existing.updateFromCrawl(category, title, content, writer, crawledDate);
        existing.updateViewCount(viewCount);

        // 첨부파일 재동기화
        crawledAnnouncementFileRepository.deleteAll(
                crawledAnnouncementFileRepository.findByCrawledAnnouncementId(id));
        for (CrawledAnnouncementFile file : newFiles) {
            file.updateCrawledAnnouncement(existing);
            crawledAnnouncementFileRepository.save(file);
        }

        // 이전 AI 생성 일정 삭제 (수동 등록 일정은 보존)
        calenderRepository.deleteAiGeneratedBySourceId(id);
    }
}