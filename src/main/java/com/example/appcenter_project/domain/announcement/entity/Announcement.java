package com.example.appcenter_project.domain.announcement.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.common.file.entity.AttachedFile;
import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
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

    @Enumerated(EnumType.STRING)
    protected AnnouncementCategory announcementCategory;

    @Enumerated(EnumType.STRING)
    private AnnouncementType announcementType;

    protected String title;
    protected String writer;
    protected int viewCount = 0;

    @Lob
    protected String content;

    public Announcement(String title, String writer, int viewCount, AnnouncementType announcementType, AnnouncementCategory category) {
        this.announcementCategory = category;
        this.title = title;
        this.writer = writer;
        this.viewCount = viewCount;
        this.announcementType = announcementType;
    }

    public abstract LocalDateTime getSortDate();

    public abstract void update(RequestAnnouncementDto requestAnnouncementDto);
}
