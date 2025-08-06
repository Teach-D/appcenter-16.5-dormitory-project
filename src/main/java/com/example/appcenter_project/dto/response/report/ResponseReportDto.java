package com.example.appcenter_project.dto.response.report;

import com.example.appcenter_project.entity.report.Report;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseReportDto {

    private Long id;
    private String category;
    private String title;
    private String content;

    public static ResponseReportDto entityToDto(Report report) {
        return ResponseReportDto.builder()
                .id(report.getId())
                .category(report.getCategory())
                .title(report.getTitle())
                .content(report.getContent())
                .build();
    }
}
