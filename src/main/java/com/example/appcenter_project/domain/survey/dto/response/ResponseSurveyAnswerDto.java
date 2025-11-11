package com.example.appcenter_project.domain.survey.dto.response;

import com.example.appcenter_project.domain.survey.entiity.SurveyAnswer;
import com.example.appcenter_project.domain.survey.enums.QuestionType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseSurveyAnswerDto {

    private Long id;
    private Long questionId;
    private String questionText;
    private QuestionType questionType;

    @Builder.Default
    private List<SelectedOptionDto> selectedOptions = new ArrayList<>();  // 선택된 옵션들 (다중 선택)

    private String answerText;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Getter
    public static class SelectedOptionDto {
        private Long optionId;
        private String optionText;
    }

    public static ResponseSurveyAnswerDto entityToDto(SurveyAnswer answer) {
        List<SelectedOptionDto> selectedOptionDtos = answer.getSelectedOptions().stream()
                .map(option -> new SelectedOptionDto(option.getId(), option.getOptionText()))
                .collect(Collectors.toList());

        return ResponseSurveyAnswerDto.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion().getId())
                .questionText(answer.getQuestion().getQuestionText())
                .questionType(answer.getQuestion().getQuestionType())
                .selectedOptions(selectedOptionDtos)
                .answerText(answer.getAnswerText())
                .build();
    }
}

