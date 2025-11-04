package com.example.appcenter_project.domain.survey.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSurveyOptionDto {

    private Long optionId;  // 수정 시 기존 옵션 ID (없으면 신규 생성)

    @NotBlank(message = "선택지 내용은 필수입니다.")
    private String optionText;

    @NotNull(message = "선택지 순서는 필수입니다.")
    private Integer optionOrder;
}

