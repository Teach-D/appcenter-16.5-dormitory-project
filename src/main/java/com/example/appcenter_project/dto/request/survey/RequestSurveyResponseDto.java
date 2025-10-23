package com.example.appcenter_project.dto.request.survey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RequestSurveyResponseDto {

    @NotNull(message = "설문 ID는 필수입니다.")
    private Long surveyId;

    @Valid
    private List<RequestSurveyAnswerDto> answers = new ArrayList<>();
}

