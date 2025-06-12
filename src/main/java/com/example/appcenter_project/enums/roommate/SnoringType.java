package com.example.appcenter_project.enums.roommate;

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

    @JsonCreator
    public static SnoringType from(String value) {
        for (SnoringType type : SnoringType.values()) {
            if (type.getDescription().equals(value)) {
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