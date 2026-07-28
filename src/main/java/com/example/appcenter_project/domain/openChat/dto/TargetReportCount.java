package com.example.appcenter_project.domain.openChat.dto;

import lombok.Getter;

@Getter
public class TargetReportCount {
    private final String studentNumber;
    private final long count;

    public TargetReportCount(String studentNumber, Long count) {
        this.studentNumber = studentNumber;
        this.count = count != null ? count : 0L;
    }
}