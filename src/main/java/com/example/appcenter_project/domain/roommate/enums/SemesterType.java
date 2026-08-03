package com.example.appcenter_project.domain.roommate.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SemesterType {
    FIRST(1),
    SECOND(2),
    SUMMER_VACATION(3),
    WINTER_VACATION(4);

    private final int code;

    SemesterType(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator
    public static SemesterType fromCode(int code) {
        for (SemesterType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Unknown semester code: " + code);
    }

    public static SemesterType fromCodeOrNull(Integer code) {
        if (code == null) return null;
        for (SemesterType type : values()) {
            if (type.code == code) return type;
        }
        return null;
    }
}
