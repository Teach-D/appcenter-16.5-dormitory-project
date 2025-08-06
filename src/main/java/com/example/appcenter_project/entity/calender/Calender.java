package com.example.appcenter_project.entity.calender;

import com.example.appcenter_project.dto.request.calender.RequestCalenderDto;
import com.example.appcenter_project.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Calender extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String link;

    @Builder
    public Calender(LocalDate startDate, LocalDate endDate, String title, String link) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
        this.link = link;
    }

    public void update(RequestCalenderDto requestCalenderDto) {
        this.startDate = LocalDate.parse(requestCalenderDto.getStartDate());
        this.endDate = LocalDate.parse(requestCalenderDto.getEndDate());
        this.title = requestCalenderDto.getTitle();
        this.link = requestCalenderDto.getLink();
    }
}
