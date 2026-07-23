package com.example.appcenter_project.domain.roommate.fixture;

import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;

public class RoommateSemesterFixture {

    public static ResponseRoommatePostDto createResponseWithSemester(Integer year, Integer semester) {
        return ResponseRoommatePostDto.builder()
                .id(1L)
                .year(year)
                .semester(semester)
                .build();
    }
}
