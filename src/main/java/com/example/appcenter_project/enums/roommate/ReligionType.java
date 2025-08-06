package com.example.appcenter_project.enums.roommate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReligionType {
    CHRISTIAN("기독교"),
    BUDDHIST("불교"),
    CATHOLIC("천주교"),
    ISLAM("이슬람교"),
    HINDU("힌두교"),
    JEWISH("유대교"),
    NONE("무교"),
    OTHER("기타");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ReligionType from(String value) {
        for (ReligionType type : ReligionType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ReligionType: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
