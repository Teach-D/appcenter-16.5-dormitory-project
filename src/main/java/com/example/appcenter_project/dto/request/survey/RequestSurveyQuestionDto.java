package com.example.appcenter_project.dto.request.survey;

import com.example.appcenter_project.enums.survey.QuestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RequestSurveyQuestionDto {

    @NotBlank(message = "질문 내용은 필수입니다.")
    private String questionText;

    @NotNull(message = "질문 타입은 필수입니다.")
    private QuestionType questionType;

    @NotNull(message = "질문 순서는 필수입니다.")
    private Integer questionOrder;

    @JsonProperty("isRequired")
    private boolean isRequired = false;

    @JsonProperty("allowMultipleSelection")
    private boolean allowMultipleSelection = false;  // 객관식일 경우 다중 선택 허용 여부

    @Valid
    private List<RequestSurveyOptionDto> options = new ArrayList<>();
}

