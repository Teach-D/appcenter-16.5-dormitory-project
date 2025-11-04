package com.example.appcenter_project.domain.survey.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RequestSurveyResponseDto {

    @NotNull(message = "설문 ID는 필수입니다.")
    private Long surveyId;

    @Valid
    private List<RequestSurveyAnswerDto> answers = new ArrayList<>();
}

