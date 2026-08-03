package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import lombok.Getter;

@Getter
public class ResponseRoommateMatchingStatusDto {
    private final int year;
    private final SemesterType semester;
    private final RoommateMatchingStatus status;

    private ResponseRoommateMatchingStatusDto(int year, SemesterType semester, RoommateMatchingStatus status) {
        this.year = year;
        this.semester = semester;
        this.status = status;
    }

    public static ResponseRoommateMatchingStatusDto of(int year, SemesterType semester, RoommateMatchingStatus status) {
        return new ResponseRoommateMatchingStatusDto(year, semester, status);
    }
}