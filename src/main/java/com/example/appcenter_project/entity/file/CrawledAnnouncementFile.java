package com.example.appcenter_project.entity.file;

import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.announcement.CrawledAnnouncement;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
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
