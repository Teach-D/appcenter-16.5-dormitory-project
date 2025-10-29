package com.example.appcenter_project.dto.response.survey;

import com.example.appcenter_project.entity.survey.Survey;
import com.example.appcenter_project.enums.survey.SurveyStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseSurveyDetailDto {

    private Long id;
    private String title;
    private String description;
    private String creatorName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SurveyStatus status;
    private LocalDateTime createdDate;
    private Integer totalResponses;
    private boolean hasSubmitted = false;  // 사용자 제출 여부

    @Builder.Default
    private List<ResponseSurveyQuestionDto> questions = new ArrayList<>();

    public static ResponseSurveyDetailDto entityToDto(Survey survey, boolean hasSubmitted) {
        return ResponseSurveyDetailDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .creatorName(survey.getCreator().getName())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .status(survey.getStatus())
                .createdDate(survey.getCreatedDate())
                .totalResponses(survey.getResponses().size())
                .hasSubmitted(hasSubmitted)
                .questions(survey.getQuestions().stream()
                        .map(ResponseSurveyQuestionDto::entityToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}

