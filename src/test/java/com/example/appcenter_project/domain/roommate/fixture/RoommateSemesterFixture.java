package com.example.appcenter_project.domain.roommate.fixture;

import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;

public class RoommateSemesterFixture {

    public static ResponseRoommatePostDto createResponseWithSemester(Integer year, SemesterType semester) {
        return ResponseRoommatePostDto.builder()
                .id(1L)
                .year(year)
                .semester(semester)
                .build();
    }
}
