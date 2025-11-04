package com.example.appcenter_project.domain.complaint.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DormBuilding {

    A("A동"),
    B("B동");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DormBuilding from(String value) {
        for (DormBuilding building : DormBuilding.values()) {
            if (building.getDescription().equals(value) || building.name().equalsIgnoreCase(value)) {
                return building;
            }
        }
        throw new IllegalArgumentException("Invalid DormBuilding: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
