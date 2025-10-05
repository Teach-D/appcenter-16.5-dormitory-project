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

    private int NO;
    private int number;

    private String category;

    @Lob
    private String content;

    @OneToMany(mappedBy = "crawledAnnouncement")
    private List<CrawledAnnouncementFile> crawledAnnouncementFiles = new ArrayList<>();

    @Builder
    public CrawledAnnouncement(Long id, String title, String writer, int viewCount, AnnouncementType announcementType, int NO, int number, String category, String content, List<CrawledAnnouncementFile> crawledAnnouncementFiles) {
        super(id, title, writer, viewCount, announcementType);
        this.NO = NO;
        this.number = number;
        this.category = category;
        this.content = content;
        this.crawledAnnouncementFiles = crawledAnnouncementFiles;
    }

    public CrawledAnnouncement(int NO, int number, String category, String content, List<CrawledAnnouncementFile> crawledAnnouncementFiles) {
        this.NO = NO;
        this.number = number;
        this.category = category;
        this.content = content;
        this.crawledAnnouncementFiles = crawledAnnouncementFiles;
    }

    public CrawledAnnouncement(String title, String writer, int viewCount, AnnouncementType announcementType, List<AttachedFile> attachedFiles, int NO, int number, String category, String content, List<CrawledAnnouncementFile> crawledAnnouncementFiles) {
        super(title, writer, viewCount, announcementType, attachedFiles);
        this.NO = NO;
        this.number = number;
        this.category = category;
        this.content = content;
        this.crawledAnnouncementFiles = crawledAnnouncementFiles;
    }

    public void updateCreateDate(LocalDate createDate) {
        super.createdDate = createdDate;
    }
}
