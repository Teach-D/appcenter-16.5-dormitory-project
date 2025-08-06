package com.example.appcenter_project.enums.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DormType {
    DORM_1("1기숙사"),
    DORM_2("2기숙사"),
    DORM_3("3기숙사");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DormType from(String value) {
        for (DormType type : DormType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid DormType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
