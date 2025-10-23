package com.example.appcenter_project.dto.request.survey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RequestSurveyOptionDto {

    @NotBlank(message = "선택지 내용은 필수입니다.")
    private String optionText;

    @NotNull(message = "선택지 순서는 필수입니다.")
    private Integer optionOrder;
}

