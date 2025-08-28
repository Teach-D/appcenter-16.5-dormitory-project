package com.example.appcenter_project.dto.request.calender;

import com.example.appcenter_project.entity.calender.Calender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Schema(description = "캘린더 요청 DTO")
@Getter
@Slf4j
public class RequestCalenderDto {

    @Schema(description = "시작 날짜", example = "2025-08-05", required = true)
    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2025-08-05", required = true)
    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;

    @Schema(description = "캘린더 제목", example = "중간고사", required = true)
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @Schema(description = "관련 링크", example = "https://example.com")
    @Size(max = 500, message = "링크는 500자 이하여야 합니다.")
    private String link;

    public static Calender dtoToEntity(RequestCalenderDto requestCalenderDto) {
        log.info(String.valueOf(requestCalenderDto.getStartDate()));
        log.info(String.valueOf(requestCalenderDto.getEndDate()));

        return Calender.builder()
                .startDate(requestCalenderDto.getStartDate())
                .endDate(requestCalenderDto.getEndDate())
                .title(requestCalenderDto.getTitle())
                .link(requestCalenderDto.getLink())
                .build();
    }
}
