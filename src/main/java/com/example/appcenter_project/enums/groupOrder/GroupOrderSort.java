package com.example.appcenter_project.enums.groupOrder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupOrderSort {

    DEADLINE("마감임박순"),
    PRICE("가격순"),
    POPULARITY("조회순");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GroupOrderSort from(String value) {
        for (GroupOrderSort type : GroupOrderSort.values()) {
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
