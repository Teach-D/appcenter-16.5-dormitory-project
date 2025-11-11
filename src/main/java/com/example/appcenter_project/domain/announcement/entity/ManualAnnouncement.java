package com.example.appcenter_project.domain.announcement.entity;

import com.example.appcenter_project.common.file.entity.AttachedFile;
import com.example.appcenter_project.common.file.entity.ManualAnnouncementFile;
import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ManualAnnouncement extends Announcement {

    private boolean isEmergency = false;

    @OneToMany(mappedBy = "manualAnnouncement", cascade = CascadeType.REMOVE)
    private List<ManualAnnouncementFile> manualAnnouncementFiles = new ArrayList<>();

    @Builder
    public ManualAnnouncement(Long id, AnnouncementCategory category, String title, String writer, int viewCount, AnnouncementType announcementType, boolean isEmergency, String content, List<ManualAnnouncementFile> manualAnnouncementFiles) {
        super(id, category, announcementType, title, writer, viewCount, content);
        this.isEmergency = isEmergency;
        this.manualAnnouncementFiles = manualAnnouncementFiles;
    }

    public void plusViewCount() {
        super.viewCount += 1;
    }

    @Override
    public LocalDateTime getSortDate() {
        return getCreatedDate();
    }

    public void update(RequestAnnouncementDto requestAnnouncementDto) {
        super.announcementCategory = requestAnnouncementDto.getCategory();
        this.title = requestAnnouncementDto.getTitle();
        this.writer = requestAnnouncementDto.getWriter();
        super.content = requestAnnouncementDto.getContent();
        this.isEmergency = requestAnnouncementDto.getIsEmergency();
    }

    public static ManualAnnouncement of(RequestAnnouncementDto requestAnnouncementDto, AnnouncementType announcementType) {
        return ManualAnnouncement.builder()
                .category(requestAnnouncementDto.getCategory())
                .title(requestAnnouncementDto.getTitle())
                .writer(requestAnnouncementDto.getWriter())
                .content(requestAnnouncementDto.getContent())
                .isEmergency(requestAnnouncementDto.getIsEmergency())
                .announcementType(announcementType)
                .build();
    }
}
