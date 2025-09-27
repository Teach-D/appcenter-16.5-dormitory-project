package com.example.appcenter_project.enums.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    ROLE_ADMIN("일반사용자"),
    ROLE_USER("관리자"),
    DORMITORY_MANAGER("기숙사 담당자"),
    DORMITORY_LIFE_MANAGER("기숙사 생활민원 담당자"),
    DORMITORY_ROOMMATE_MANAGER("기숙사 룸메이트민원 담당자"),
    DORMITORY_SUPPORTERS("기숙사 서포터즈");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Role from(String value) {
        for (Role type : Role.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid Role: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
