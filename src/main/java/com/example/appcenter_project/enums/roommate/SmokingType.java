package com.example.appcenter_project.enums.roommate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SmokingType {
    SMOKER("피워요"),
    NON_SMOKER("안피워요");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SmokingType from(String value) {
        for (SmokingType type : SmokingType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid SmokingType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}