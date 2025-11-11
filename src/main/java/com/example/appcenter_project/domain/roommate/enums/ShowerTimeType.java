package com.example.appcenter_project.domain.roommate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShowerTimeType {
    MORNING("아침"),
    EVENING("저녁"),
    BOTH("둘다");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ShowerTimeType from(String value) {
        for (ShowerTimeType type : ShowerTimeType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ShowerTimeType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
