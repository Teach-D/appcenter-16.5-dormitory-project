package com.example.appcenter_project.dto.response.survey;

import com.example.appcenter_project.entity.survey.Survey;
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
    private boolean isClosed;
    private LocalDateTime createdDate;
    private Integer totalResponses;

    @Builder.Default
    private List<ResponseSurveyQuestionDto> questions = new ArrayList<>();

    public static ResponseSurveyDetailDto entityToDto(Survey survey) {
        return ResponseSurveyDetailDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .creatorName(survey.getCreator().getName())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .isClosed(survey.isClosed())
                .createdDate(survey.getCreatedDate())
                .totalResponses(survey.getResponses().size())
                .questions(survey.getQuestions().stream()
                        .map(ResponseSurveyQuestionDto::entityToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}

