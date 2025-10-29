package com.example.appcenter_project.dto.request.survey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RequestSurveyDto {

    @NotBlank(message = "설문 제목은 필수입니다.")
    private String title;

    private String description;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDateTime endDate;

    private int recruitmentCount;

    @Valid
    private List<RequestSurveyQuestionDto> questions = new ArrayList<>();
}

