package com.example.appcenter_project.enums.complaint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintType {

    NOISE("소음"),
    SMOKING("흡연"),
    DRINKING("음주"),
    ROOMMATE_CHANGE("룸메이트변경"),
    POINT_OFFSET("벌점상쇄"),
    POINT_REASON_CHECK("벌점사유확인");

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