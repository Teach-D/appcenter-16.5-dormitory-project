package com.example.appcenter_project.domain.announcement.entity;

import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.common.file.entity.AttachedFile;
import com.example.appcenter_project.common.file.entity.ManualAnnouncementFile;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
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
public class ManualAnnouncement extends Announcement {

    private boolean isEmergency = false;

    @Lob
    private String content;

    @OneToMany(mappedBy = "manualAnnouncement", cascade = CascadeType.REMOVE)
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

    @Override
    public LocalDateTime getSortDate() {
        return getCreatedDate();
    }

    @Override
    public void update(RequestAnnouncementDto requestAnnouncementDto) {
        this.title = requestAnnouncementDto.getTitle();
        this.writer = requestAnnouncementDto.getWriter();
        this.content = requestAnnouncementDto.getContent();
        this.isEmergency = requestAnnouncementDto.getIsEmergency();
    }
}
