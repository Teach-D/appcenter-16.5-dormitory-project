package com.example.appcenter_project.entity.announcement;

import com.example.appcenter_project.dto.request.announement.RequestAnnouncementDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    private String number;
    private String title;
    private String writer;
    private int viewCount = 0;
    private Boolean isEmergency = false;

    // 크롤링한 게시글의 등록 날짜
    private LocalDate crawlCreateDate;

    @Enumerated(EnumType.STRING)
    private AnnouncementType announcementType;

    @Lob
    private String content;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedFile> attachedFiles = new ArrayList<>();

    @Builder
    public Announcement(String number, String title, String writer, int viewCount, LocalDate crawlCreateDate,
                        String content, boolean isEmergency, AnnouncementType announcementType, List<AttachedFile> attachedFiles) {
        this.number = number;
        this.title = title;
        this.writer = writer;
        this.viewCount = viewCount;
        this.content = content;
        this.isEmergency = isEmergency;
        this.crawlCreateDate = crawlCreateDate;
        this.announcementType = announcementType;
        this.attachedFiles = attachedFiles;
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
