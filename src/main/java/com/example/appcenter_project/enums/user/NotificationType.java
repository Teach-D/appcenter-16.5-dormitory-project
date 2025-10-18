package com.example.appcenter_project.enums.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    ROOMMATE("룸메이트"),
    GROUP_ORDER("공동구매"),
    DORMITORY("생활원"),
    UNI_DORM("유니돔"),
    SUPPORTERS("서포터즈"),
    COMPLAINT("민원"),
    COUPON("쿠폰");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static NotificationType from(String value) {
        for (NotificationType type : NotificationType.values()) {
            if (type.getDescription().equals(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("NotificationType College: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
