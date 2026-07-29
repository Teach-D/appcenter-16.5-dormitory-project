package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.notification.service.RoommateNotificationService;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.repository.*;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.mixpanel.MixpanelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoommateSemesterServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoommateCheckListRepository roommateCheckListRepository;
    @Mock RoommateBoardRepository roommateBoardRepository;
    @Mock RoommateBoardLikeRepository roommateBoardLikeRepository;
    @Mock RoommateMatchingRepository roommateMatchingRepository;
    @Mock RoommateChattingRoomRepository roommateChattingRoomRepository;
    @Mock ImageService imageService;
    @Mock RoommateNotificationService roommateNotificationService;
    @Mock MixpanelService mixpanelService;

    @InjectMocks RoommateService roommateService;

    // --- AC-1: 1·2월 → FIRST ---

    @Test
    @DisplayName("AC-1: 1월 입력 시 FIRST 반환")
    void resolveSemester_월이_1일때_FIRST() {
        assertThat(roommateService.resolveSemester(1)).isEqualTo(SemesterType.FIRST);
    }

    @Test
    @DisplayName("AC-1: 2월 입력 시 FIRST 반환")
    void resolveSemester_월이_2일때_FIRST() {
        assertThat(roommateService.resolveSemester(2)).isEqualTo(SemesterType.FIRST);
    }

    // --- AC-2: 7·8월 → SECOND ---

    @Test
    @DisplayName("AC-2: 7월 입력 시 SECOND 반환")
    void resolveSemester_월이_7일때_SECOND() {
        assertThat(roommateService.resolveSemester(7)).isEqualTo(SemesterType.SECOND);
    }

    @Test
    @DisplayName("AC-2: 8월 입력 시 SECOND 반환")
    void resolveSemester_월이_8일때_SECOND() {
        assertThat(roommateService.resolveSemester(8)).isEqualTo(SemesterType.SECOND);
    }

    // --- AC-3: 5월 → SUMMER_VACATION ---

    @Test
    @DisplayName("AC-3: 5월 입력 시 SUMMER_VACATION 반환")
    void resolveSemester_월이_5일때_SUMMER_VACATION() {
        assertThat(roommateService.resolveSemester(5)).isEqualTo(SemesterType.SUMMER_VACATION);
    }

    // --- AC-4: 11월 → WINTER_VACATION ---

    @Test
    @DisplayName("AC-4: 11월 입력 시 WINTER_VACATION 반환")
    void resolveSemester_월이_11일때_WINTER_VACATION() {
        assertThat(roommateService.resolveSemester(11)).isEqualTo(SemesterType.WINTER_VACATION);
    }

    // --- AC-5: 그 외 월 → null ---

    @Test
    @DisplayName("AC-5: 3월 입력 시 null 반환")
    void resolveSemester_월이_3일때_null() {
        assertThat(roommateService.resolveSemester(3)).isNull();
    }

    @Test
    @DisplayName("AC-5: 6월 입력 시 null 반환")
    void resolveSemester_월이_6일때_null() {
        assertThat(roommateService.resolveSemester(6)).isNull();
    }

    @Test
    @DisplayName("AC-5: 10월 입력 시 null 반환")
    void resolveSemester_월이_10일때_null() {
        assertThat(roommateService.resolveSemester(10)).isNull();
    }

    // --- AC-6: entityToDto 매핑 검증 ---

    @Test
    @DisplayName("AC-6: entityToDto 변환 시 board.year가 응답에 포함")
    void entityToDto_board의_year가_응답에_포함() {
        User user = User.createForTest(1L, "테스트유저", DormType.DORM_1);
        RoommateCheckList checkList = mock(RoommateCheckList.class);
        when(checkList.getDormPeriod()).thenReturn(Collections.emptySet());
        RoommateBoard board = mock(RoommateBoard.class);
        when(board.getUser()).thenReturn(user);
        when(board.getRoommateCheckList()).thenReturn(checkList);
        when(board.getYear()).thenReturn(2026);
        when(board.getSemester()).thenReturn(SemesterType.FIRST);

        ResponseRoommatePostDto result = ResponseRoommatePostDto.entityToDto(board, false, null);

        assertThat(result.getYear()).isEqualTo(2026);
    }

    @Test
    @DisplayName("AC-6: entityToDto 변환 시 board.semester가 응답에 포함")
    void entityToDto_board의_semester가_응답에_포함() {
        User user = User.createForTest(1L, "테스트유저", DormType.DORM_1);
        RoommateCheckList checkList = mock(RoommateCheckList.class);
        when(checkList.getDormPeriod()).thenReturn(Collections.emptySet());
        RoommateBoard board = mock(RoommateBoard.class);
        when(board.getUser()).thenReturn(user);
        when(board.getRoommateCheckList()).thenReturn(checkList);
        when(board.getYear()).thenReturn(2026);
        when(board.getSemester()).thenReturn(SemesterType.FIRST);

        ResponseRoommatePostDto result = ResponseRoommatePostDto.entityToDto(board, false, null);

        assertThat(result.getSemester()).isEqualTo(SemesterType.FIRST);
    }

    @Test
    @DisplayName("AC-6: 그 외 월 게시글 entityToDto 변환 시 year=null, semester=null")
    void entityToDto_year_semester가_null인_board는_응답도_null() {
        User user = User.createForTest(1L, "테스트유저", DormType.DORM_1);
        RoommateCheckList checkList = mock(RoommateCheckList.class);
        when(checkList.getDormPeriod()).thenReturn(Collections.emptySet());
        RoommateBoard board = mock(RoommateBoard.class);
        when(board.getUser()).thenReturn(user);
        when(board.getRoommateCheckList()).thenReturn(checkList);
        when(board.getYear()).thenReturn(null);
        when(board.getSemester()).thenReturn(null);

        ResponseRoommatePostDto result = ResponseRoommatePostDto.entityToDto(board, false, null);

        assertThat(result.getYear()).isNull();
        assertThat(result.getSemester()).isNull();
    }
}
