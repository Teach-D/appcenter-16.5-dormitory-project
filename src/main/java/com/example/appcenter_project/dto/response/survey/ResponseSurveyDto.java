package com.example.appcenter_project.dto.response.survey;

import com.example.appcenter_project.entity.survey.Survey;
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
    private boolean isClosed;
    private LocalDateTime createdDate;
    private Integer totalResponses;

    public static ResponseSurveyDto entityToDto(Survey survey) {
        return ResponseSurveyDto.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .creatorName(survey.getCreator().getName())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .isClosed(survey.isClosed())
                .createdDate(survey.getCreatedDate())
                .totalResponses(survey.getResponses().size())
                .build();
    }
}

