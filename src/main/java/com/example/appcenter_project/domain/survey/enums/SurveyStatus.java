package com.example.appcenter_project.domain.survey.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SurveyStatus {
    BEFORE("진행전"),      // 시작일 이전
    PROCEEDING("진행중"),  // 진행 중
    CLOSED("마감");        // 종료됨
    
    private final String description;
    
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SurveyStatus from(String value) {
        for (SurveyStatus status : SurveyStatus.values()) {
            if (status.getDescription().equals(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid SurveyStatus: " + value);
    }
    
    @JsonValue
    public String toValue() {
        return this.description;
    }
}

