package com.example.appcenter_project.common.file.entity;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.complaint.entity.ComplaintReply;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class CrawledAnnouncementFile extends AttachedFile {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crawled_announement_id")
    private CrawledAnnouncement crawledAnnouncement;

    public CrawledAnnouncementFile(CrawledAnnouncement crawledAnnouncement) {
        this.crawledAnnouncement = crawledAnnouncement;
    }

    @Builder
    public CrawledAnnouncementFile(String filePath, String fileName, Long fileSize, Announcement announcement, ComplaintReply complaintReply, CrawledAnnouncement crawledAnnouncement) {
        super(filePath, fileName, fileSize, announcement, complaintReply);
        this.crawledAnnouncement = crawledAnnouncement;
    }

    public void updateCrawledAnnouncement(CrawledAnnouncement crawledAnnouncement) {
        this.crawledAnnouncement = crawledAnnouncement;
    }
}
