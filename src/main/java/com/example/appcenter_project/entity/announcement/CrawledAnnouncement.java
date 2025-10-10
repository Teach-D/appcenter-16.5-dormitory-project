package com.example.appcenter_project.entity.announcement;

import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.entity.file.CrawledAnnouncementFile;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class CrawledAnnouncement extends Announcement {

    private String number;

    private String category;

    @Lob
    private String content;

    @OneToMany(mappedBy = "crawledAnnouncement", cascade = CascadeType.REMOVE)
    private List<CrawledAnnouncementFile> crawledAnnouncementFiles = new ArrayList<>();

    private LocalDate crawledDate;

    private String link;

    @Builder
    public CrawledAnnouncement(Long id, String title, String writer, int viewCount, AnnouncementType announcementType, String number, String category, String content, List<CrawledAnnouncementFile> crawledAnnouncementFiles, LocalDate crawledDate, String link) {
        super(id, title, writer, viewCount, announcementType);
        this.number = number;
        this.category = category;
        this.content = content;
        this.crawledAnnouncementFiles = crawledAnnouncementFiles;
        this.crawledDate = crawledDate;
        this.link = link;
    }

    public CrawledAnnouncement(int NO, String number, String category, String content, List<CrawledAnnouncementFile> crawledAnnouncementFiles) {
        this.number = number;
        this.category = category;
        this.content = content;
        this.crawledAnnouncementFiles = crawledAnnouncementFiles;
    }

    public CrawledAnnouncement(String title, String writer, int viewCount, AnnouncementType announcementType, List<AttachedFile> attachedFiles, String number, String category, String content, List<CrawledAnnouncementFile> crawledAnnouncementFiles) {
        super(title, writer, viewCount, announcementType, attachedFiles);
        this.number = number;
        this.category = category;
        this.content = content;
        this.crawledAnnouncementFiles = crawledAnnouncementFiles;
    }
}
