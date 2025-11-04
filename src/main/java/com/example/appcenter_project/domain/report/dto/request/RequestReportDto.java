package com.example.appcenter_project.domain.report.dto.request;

import com.example.appcenter_project.domain.report.entity.Report;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RequestReportDto {

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    public static Report dtoToEntity(RequestReportDto requestReportDto) {
        return Report.builder()
                .category(requestReportDto.getCategory())
                .title(requestReportDto.getTitle())
                .content(requestReportDto.getContent())
                .build();
    }
}
