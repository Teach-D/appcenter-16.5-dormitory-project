package com.example.appcenter_project.domain.announcement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnnouncementCategory {

    ALL("전체"),
    LIFE_GUIDANCE("생활지도"),
    FACILITY("시설"),
    EVENT_LECTURE("행사/강좌"),
    BTL_DORMITORY("BTL기숙사"),
    MOVE_IN_OUT("입퇴사 공지"),
    ETC("기타");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AnnouncementCategory from(String value) {
        for (AnnouncementCategory type : AnnouncementCategory.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid AnnouncementCategory: " + value);
    }

    public String toValue() {
        return this.description;
    }
}