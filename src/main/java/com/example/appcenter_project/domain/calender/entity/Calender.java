package com.example.appcenter_project.domain.calender.entity;

import com.example.appcenter_project.domain.calender.dto.request.RequestCalenderDto;
import com.example.appcenter_project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
public class Calender extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String link;

    @Column(name = "source_announcement_id")
    private Long sourceAnnouncementId;

    @Column(name = "ai_generated")
    private boolean aiGenerated = false;

    @Builder
    public Calender(LocalDate startDate, LocalDate endDate, String title, String link) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.link = link;
    }

    public void update(RequestCalenderDto dto) {
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.title = dto.getTitle();
        this.link = dto.getLink();
    }

    public static Calender from(RequestCalenderDto requestDto) {
        return Calender.builder()
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .title(requestDto.getTitle())
                .link(requestDto.getLink())
                .build();
    }

    public static Calender ofAiGenerated(LocalDate startDate, LocalDate endDate,
                                         String title, String link, Long sourceAnnouncementId) {
        Calender calender = Calender.builder()
                .startDate(startDate)
                .endDate(endDate)
                .title(title)
                .link(link)
                .build();
        calender.sourceAnnouncementId = sourceAnnouncementId;
        calender.aiGenerated = true;
        return calender;
    }
}