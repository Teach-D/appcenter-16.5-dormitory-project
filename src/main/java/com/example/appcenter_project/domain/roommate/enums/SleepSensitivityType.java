package com.example.appcenter_project.domain.roommate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SleepSensitivityType {
    SENSITIVE_TO_LIGHT("밝아요"),
    PREFER_DARKNESS("어두워요"),
    NOT_SURE("몰라요");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SleepSensitivityType from(String value) {
        for (SleepSensitivityType type : SleepSensitivityType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid SleepSensitivityType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
