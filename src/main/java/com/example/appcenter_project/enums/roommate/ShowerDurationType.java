package com.example.appcenter_project.enums.roommate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShowerDurationType {
    WITHIN_10_MINUTES("10분 이내"),
    WITHIN_30_MINUTES("30분 이내"),
    WITHIN_1_HOUR("1시간 이내");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ShowerDurationType from(String value) {
        for (ShowerDurationType type : ShowerDurationType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ShowerDurationType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
