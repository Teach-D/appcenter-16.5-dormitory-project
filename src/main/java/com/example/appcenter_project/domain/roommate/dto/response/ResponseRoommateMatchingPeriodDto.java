package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ResponseRoommateMatchingPeriodDto {
    private final int year;
    private final SemesterType semester;   // 정수(1~4)로 직렬화 (@JsonValue)
    private final String label;            // 예: "2026년 2학기"

    @JsonProperty("isCurrent")
    private final boolean current;         // 현재 매칭 기간 여부

    private ResponseRoommateMatchingPeriodDto(int year, SemesterType semester, String label, boolean current) {
        this.year = year;
        this.semester = semester;
        this.label = label;
        this.current = current;
    }

    public static ResponseRoommateMatchingPeriodDto of(int year, SemesterType semester, boolean current) {
        return new ResponseRoommateMatchingPeriodDto(year, semester, buildLabel(year, semester), current);
    }

    private static String buildLabel(int year, SemesterType semester) {
        String suffix = switch (semester) {
            case FIRST -> "1학기";
            case SECOND -> "2학기";
            case SUMMER_VACATION -> "여름방학";
            case WINTER_VACATION -> "겨울방학";
        };
        return year + "년 " + suffix;
    }
}