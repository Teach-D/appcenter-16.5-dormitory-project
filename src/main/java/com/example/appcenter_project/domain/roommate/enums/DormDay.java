package com.example.appcenter_project.domain.roommate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DormDay {
    MON("월"),
    TUE("화"),
    WED("수"),
    THU("목"),
    FRI("금"),
    SAT("토"),
    SUN("일");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DormDay from(String value) {
        for (DormDay day : DormDay.values()) {
            if (day.getDescription().equals(value) || day.name().equalsIgnoreCase(value)) {
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
