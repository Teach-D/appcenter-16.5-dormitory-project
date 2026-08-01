package com.example.appcenter_project.domain.roommate.fixture;

import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.user.entity.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoommateMultiSemesterFixture {

    public static User createMockUserWithId(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn("사용자" + id);
        return user;
    }

    public static ResponseRoommatePostDto createBoardResponse(Long boardId, Integer year, SemesterType semester) {
        return ResponseRoommatePostDto.builder()
                .id(boardId)
                .year(year)
                .semester(semester)
                .build();
    }
}
