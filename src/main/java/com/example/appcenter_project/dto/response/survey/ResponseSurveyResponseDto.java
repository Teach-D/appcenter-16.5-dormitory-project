package com.example.appcenter_project.dto.response.survey;

import com.example.appcenter_project.entity.survey.SurveyResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseSurveyResponseDto {

    private Long id;
    private Long surveyId;
    private String surveyTitle;
    private String userName;
    private LocalDateTime submittedAt;

    @Builder.Default
    private List<ResponseSurveyAnswerDto> answers = new ArrayList<>();

    public static ResponseSurveyResponseDto entityToDto(SurveyResponse response) {
        return ResponseSurveyResponseDto.builder()
                .id(response.getId())
                .surveyId(response.getSurvey().getId())
                .surveyTitle(response.getSurvey().getTitle())
                .userName(response.getUser().getName())
                .submittedAt(response.getCreatedDate())
                .answers(response.getAnswers().stream()
                        .map(ResponseSurveyAnswerDto::entityToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}

