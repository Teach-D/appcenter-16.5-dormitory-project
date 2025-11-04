package com.example.appcenter_project.domain.survey.dto.response;

import com.example.appcenter_project.domain.survey.entiity.SurveyOption;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseSurveyOptionDto {

    private Long id;
    private String optionText;
    private Integer optionOrder;

    public static ResponseSurveyOptionDto entityToDto(SurveyOption option) {
        return ResponseSurveyOptionDto.builder()
                .id(option.getId())
                .optionText(option.getOptionText())
                .optionOrder(option.getOptionOrder())
                .build();
    }
}

