package com.example.appcenter_project.domain.announcement.entity;

import com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile;
import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class CrawledAnnouncement extends Announcement {

    private String number;

    @OneToMany(mappedBy = "crawledAnnouncement", cascade = CascadeType.REMOVE)
    private List<CrawledAnnouncementFile> crawledAnnouncementFiles = new ArrayList<>();

    private LocalDate crawledDate;

    private String link;


    @Builder
    public CrawledAnnouncement(Long id, String title, String writer, int viewCount, AnnouncementType announcementType, String number, AnnouncementCategory category, String content, List<CrawledAnnouncementFile> crawledAnnouncementFiles, LocalDate crawledDate, String link) {
        super(id, category, announcementType, title, writer, viewCount, content);
        this.number = number;
        this.announcementCategory = category;
        super.content = content;
        this.crawledAnnouncementFiles = crawledAnnouncementFiles;
        this.crawledDate = crawledDate;
        this.link = link;
    }

    public void updateViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    @Override
    public LocalDateTime getSortDate() {
        return crawledDate.atStartOfDay();
    }

    @Override
    public void update(RequestAnnouncementDto requestAnnouncementDto) {
        this.title = requestAnnouncementDto.getTitle();
        this.writer = requestAnnouncementDto.getWriter();
        this.content = requestAnnouncementDto.getContent();
    }
}
