package com.example.appcenter_project.domain.roommate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SnoringType {
    SNORER("골아요"),
    NON_SNORER("안골아요");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SnoringType from(String value) {
        for (SnoringType type : SnoringType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid SnoringType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}