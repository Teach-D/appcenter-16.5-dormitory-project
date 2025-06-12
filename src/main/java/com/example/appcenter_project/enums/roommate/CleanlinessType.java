package com.example.appcenter_project.enums.roommate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CleanlinessType {
    NEAT("깔끔해요"),
    EASYGOING("개방적이에요"),
    UNCERTAIN("애매해요");

    private final String description;

    @JsonCreator
    public static CleanlinessType from(String value) {
        for (CleanlinessType type : CleanlinessType.values()) {
            if (type.getDescription().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid CleanlinessType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
