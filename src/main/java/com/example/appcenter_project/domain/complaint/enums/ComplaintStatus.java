package com.example.appcenter_project.domain.complaint.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintStatus {

    PENDING("대기중"),
    ASSIGNED("담당자 배정"),
    IN_PROGRESS("처리중"),
    COMPLETED("처리완료"),
    REJECTION("반려");

    private final String description;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ComplaintStatus from(String value) {
        for (ComplaintStatus status : ComplaintStatus.values()) {
            if (status.getDescription().equals(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ComplaintStatus: " + value);
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
