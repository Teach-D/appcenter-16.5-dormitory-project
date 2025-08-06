package com.example.appcenter_project.entity.announcement;

import com.example.appcenter_project.dto.request.announement.RequestAnnouncementDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Announcement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String writer;
    private int viewCount = 0;
    private Boolean isEmergency = false;

    @Lob
    private String content;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedFile> attachedFiles = new ArrayList<>();

    @Builder
    public Announcement(String title, String writer, String content, boolean isEmergency) {
        this.title = title;
        this.writer = writer;
        this.content = content;
        this.isEmergency = isEmergency;
    }

    public void plusViewCount() {
        this.viewCount++;
    }

    public void update(RequestAnnouncementDto requestAnnouncementDto) {
        this.title = requestAnnouncementDto.getTitle();
        this.writer = requestAnnouncementDto.getWriter();
        this.content = requestAnnouncementDto.getContent();
        this.isEmergency = requestAnnouncementDto.getIsEmergency();
    }
}
