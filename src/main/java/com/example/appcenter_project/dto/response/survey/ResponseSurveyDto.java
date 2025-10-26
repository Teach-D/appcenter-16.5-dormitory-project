package com.example.appcenter_project.dto.response.survey;

import com.example.appcenter_project.entity.survey.Survey;
import com.example.appcenter_project.enums.survey.SurveyStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseSurveyDto {

    private Long id;
    private String title;
    private String description;
    private String creatorName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdDate;
    private Integer totalResponses;
    
    @JsonProperty("hasSubmitted")
    @Builder.Default
    private boolean hasSubmitted = false;  // 사용자 제출 여부
    
    private SurveyStatus status;  // 설문 진행 상태 (진행전/진행중/마감)

    public static ResponseSurveyDto entityToDto(Survey survey) {
        return ResponseSurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .creatorName(survey.getCreator().getName())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .createdDate(survey.getCreatedDate())
                .totalResponses(survey.getResponses().size())
                .hasSubmitted(false)
                .status(survey.getStatus())
                .build();
    }
    
    public static ResponseSurveyDto entityToDto(Survey survey, boolean hasSubmitted) {
        return ResponseSurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .creatorName(survey.getCreator().getName())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .createdDate(survey.getCreatedDate())
                .totalResponses(survey.getResponses().size())
                .hasSubmitted(hasSubmitted)
                .status(survey.getStatus())
                .build();
    }
}

