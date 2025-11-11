package com.example.appcenter_project.domain.calender.dto.response;

import com.example.appcenter_project.domain.calender.entity.Calender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Schema(description = "캘린더 응답 DTO")
@Builder
@Getter
public class ResponseCalenderDto {

    @Schema(description = "캘린더 ID", example = "1")
    private Long id;

    @Schema(description = "시작 날짜", example = "2025-08-05")
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2025-08-05")
    private LocalDate endDate;

    @Schema(description = "캘린더 제목", example = "중간고사")
    private String title;

    @Schema(description = "관련 링크", example = "https://example.com")
    private String link;

    public static ResponseCalenderDto from(Calender calender) {
        return ResponseCalenderDto.builder()
                .id(calender.getId())
                .startDate(calender.getStartDate())
                .endDate(calender.getEndDate())
                .title(calender.getTitle())
                .link(calender.getLink())
                .build();
    }
}