package com.example.appcenter_project.entity.announcement;

import com.example.appcenter_project.dto.request.announement.RequestAnnouncementDto;
import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.entity.file.CrawledAnnouncementFile;
import com.example.appcenter_project.entity.file.ManualAnnouncementFile;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ManualAnnouncement extends Announcement {

    private boolean isEmergency = false;

    @Lob
    private String content;

    @OneToMany(mappedBy = "manualAnnouncement")
    private List<ManualAnnouncementFile> manualAnnouncementFiles = new ArrayList<>();

    @Builder
    public ManualAnnouncement(Long id, String title, String writer, int viewCount, AnnouncementType announcementType, boolean isEmergency, String content, List<ManualAnnouncementFile> manualAnnouncementFiles) {
        super(id, title, writer, viewCount, announcementType);
        this.isEmergency = isEmergency;
        this.content = content;
        this.manualAnnouncementFiles = manualAnnouncementFiles;
    }

    public ManualAnnouncement(boolean isEmergency, String content, List<ManualAnnouncementFile> manualAnnouncementFiles) {
        this.isEmergency = isEmergency;
        this.content = content;
        this.manualAnnouncementFiles = manualAnnouncementFiles;
    }

    public ManualAnnouncement(String title, String writer, int viewCount, AnnouncementType announcementType, List<AttachedFile> attachedFiles, boolean isEmergency, String content, List<ManualAnnouncementFile> manualAnnouncementFiles) {
        super(title, writer, viewCount, announcementType, attachedFiles);
        this.isEmergency = isEmergency;
        this.content = content;
        this.manualAnnouncementFiles = manualAnnouncementFiles;
    }

    public void plusViewCount() {
        super.viewCount += 1;
    }
}
