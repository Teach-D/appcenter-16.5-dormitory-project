package com.example.appcenter_project.domain.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RequestSurveyAnswerDto {

    @NotNull(message = "질문 ID는 필수입니다.")
    private Long questionId;

    private List<Long> optionIds = new ArrayList<>();  // 객관식인 경우 (다중 선택 가능)

    private String answerText;  // 주관식인 경우
}

