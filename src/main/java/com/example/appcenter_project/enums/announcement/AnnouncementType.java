package com.example.appcenter_project.enums.announcement;

import com.example.appcenter_project.enums.user.NotificationType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AnnouncementType {

    DORMITORY("생활원"),
    UNI_DORM("유니돔"),
    SUPPORTERS("서포터즈");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AnnouncementType from(String value) {
        for (AnnouncementType type : AnnouncementType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("AnnouncementType College: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
