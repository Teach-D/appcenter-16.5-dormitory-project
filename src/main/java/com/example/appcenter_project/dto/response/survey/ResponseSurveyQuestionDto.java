package com.example.appcenter_project.dto.response.survey;

import com.example.appcenter_project.entity.survey.SurveyQuestion;
import com.example.appcenter_project.enums.survey.QuestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseSurveyQuestionDto {

    private Long id;
    private String questionText;
    private QuestionType questionType;
    private Integer questionOrder;
    
    @JsonProperty("isRequired")
    private boolean isRequired;
    
    @JsonProperty("allowMultipleSelection")
    private boolean allowMultipleSelection;  // 다중 선택 허용 여부

    @Builder.Default
    private List<ResponseSurveyOptionDto> options = new ArrayList<>();

    public static ResponseSurveyQuestionDto entityToDto(SurveyQuestion question) {
        return ResponseSurveyQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .questionOrder(question.getQuestionOrder())
                .isRequired(question.isRequired())
                .allowMultipleSelection(question.isAllowMultipleSelection())
                .options(question.getOptions().stream()
                        .map(ResponseSurveyOptionDto::entityToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}

