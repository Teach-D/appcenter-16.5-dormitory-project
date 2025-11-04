package com.example.appcenter_project.domain.announcement.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.common.file.entity.AttachedFile;
import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class Announcement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected String title;
    protected String writer;
    protected int viewCount = 0;

    @Enumerated(EnumType.STRING)
    private AnnouncementType announcementType;

    public Announcement(String title, String writer, int viewCount, AnnouncementType announcementType, List<AttachedFile> attachedFiles) {
        this.title = title;
        this.writer = writer;
        this.viewCount = viewCount;
        this.announcementType = announcementType;
    }

    public abstract LocalDateTime getSortDate();

    public abstract void update(RequestAnnouncementDto requestAnnouncementDto);
}
