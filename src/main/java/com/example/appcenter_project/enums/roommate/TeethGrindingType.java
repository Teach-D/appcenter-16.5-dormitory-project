package com.example.appcenter_project.enums.roommate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeethGrindingType {
    GRINDER("갈아요"),
    NON_GRINDER("안갈아요");

    private final String description;

    @JsonCreator
    public static TeethGrindingType from(String value) {
        for (TeethGrindingType type : TeethGrindingType.values()) {
            if (type.getDescription().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid TeethGrindingType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}