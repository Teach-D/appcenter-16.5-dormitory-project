package com.example.appcenter_project.enums.roommate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BedTimeType {
    EARLY_SLEEPER("일찍 자요"),
    NIGHT_OWL("늦게 자요"),
    VARIES("때마다 달라요");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BedTimeType from(String value) {
        for (BedTimeType type : BedTimeType.values()) {
            // 한글 설명 또는 enum name(대소문자 무시) 둘 다 허용
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid BedTimeType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
