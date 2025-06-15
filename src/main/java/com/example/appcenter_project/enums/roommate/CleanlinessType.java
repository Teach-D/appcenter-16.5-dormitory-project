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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CleanlinessType from(String value) {
        for (CleanlinessType type : CleanlinessType.values()) {
            // 한글 설명 또는 enum 이름(대소문자 무시) 모두 처리
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
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
