package com.example.appcenter_project.dto.request.calender;

import com.example.appcenter_project.entity.calender.Calender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Schema(description = "캘린더 요청 DTO")
@Getter
public class RequestCalenderDto {

    @Schema(description = "시작 날짜", example = "2025-08-05", required = true)
    @NotBlank(message = "시작 날짜는 필수입니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 yyyy-MM-dd 여야 합니다.")
    private String startDate;

    @Schema(description = "종료 날짜", example = "2025-08-05", required = true)
    @NotBlank(message = "종료 날짜는 필수입니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 yyyy-MM-dd 여야 합니다.")
    private String endDate;

    @Schema(description = "캘린더 제목", example = "중간고사", required = true)
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;

    @Schema(description = "관련 링크", example = "https://example.com")
    @Size(max = 500, message = "링크는 500자 이하여야 합니다.")
    private String link;

    public static Calender dtoToEntity(RequestCalenderDto requestCalenderDto) {
        return Calender.builder()
                .startDate(LocalDate.parse(requestCalenderDto.getStartDate()))
                .endDate(LocalDate.parse(requestCalenderDto.getEndDate()))
                .title(requestCalenderDto.getTitle())
                .link(requestCalenderDto.getLink())
                .build();
    }
}
