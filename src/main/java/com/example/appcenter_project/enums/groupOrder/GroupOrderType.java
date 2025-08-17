package com.example.appcenter_project.enums.groupOrder;

import com.example.appcenter_project.enums.roommate.BedTimeType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupOrderType {

    ALL("전체"),
    DELIVERY("배달"),
    GROCERY("식료품"),
    LIFE_ITEM("생활용품"),
    ETC("기타");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static GroupOrderType from(String value) {
        for (GroupOrderType type : GroupOrderType.values()) {
            // 한글 설명 또는 enum name(대소문자 무시) 둘 다 허용
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid GroupOrderType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
