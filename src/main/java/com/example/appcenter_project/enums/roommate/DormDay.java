package com.example.appcenter_project.enums.roommate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DormDay {
    MON("월요일"),
    TUE("화요일"),
    WED("수요일"),
    THU("목요일"),
    FRI("금요일"),
    SAT("토요일"),
    SUN("일요일");

    private final String description;

    @JsonCreator
    public static DormDay from(String value) {
        for (DormDay day : DormDay.values()) {
            if (day.getDescription().equals(value)) {
                return day;
            }
        }
        throw new IllegalArgumentException("Invalid DormDay: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
