package com.example.appcenter_project.domain.survey.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseSurveyResultDto {

    private Long surveyId;
    private String surveyTitle;
    private Integer totalResponses;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder.Default
    private List<QuestionResultDto> questionResults = new ArrayList<>();

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class QuestionResultDto {
        private Long questionId;
        private String questionText;
        private String questionType;

        @Builder.Default
        private List<OptionResultDto> optionResults = new ArrayList<>();

        @Builder.Default
        private List<String> shortAnswers = new ArrayList<>();
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class OptionResultDto {
        private Long optionId;
        private String optionText;
        private Integer count;
        private Double percentage;
    }
}

