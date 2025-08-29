package com.example.appcenter_project.enums.complaint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintType {

    EQUIPMENT("기물"),
    FACILITY("시설"),
    TYPE1("유형1"),
    TYPE2("유형2");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ComplaintType from(String value) {
        for (ComplaintType type : ComplaintType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ComplaintType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}