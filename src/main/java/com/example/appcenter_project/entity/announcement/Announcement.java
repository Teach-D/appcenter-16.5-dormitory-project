package com.example.appcenter_project.entity.announcement;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Announcement extends BaseTimeEntity {

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
}
