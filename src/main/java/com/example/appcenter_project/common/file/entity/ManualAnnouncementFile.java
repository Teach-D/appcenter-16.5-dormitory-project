package com.example.appcenter_project.common.file.entity;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.complaint.entity.ComplaintReply;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class ManualAnnouncementFile extends AttachedFile {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manual_announement_id")
    private ManualAnnouncement manualAnnouncement;

    public ManualAnnouncementFile(ManualAnnouncement manualAnnouncement) {
        this.manualAnnouncement = manualAnnouncement;
    }

    @Builder
    public ManualAnnouncementFile(String filePath, String fileName, Long fileSize, Announcement announcement, ComplaintReply complaintReply, ManualAnnouncement manualAnnouncement) {
        super(filePath, fileName, fileSize, announcement, complaintReply);
        this.manualAnnouncement = manualAnnouncement;
    }
}
