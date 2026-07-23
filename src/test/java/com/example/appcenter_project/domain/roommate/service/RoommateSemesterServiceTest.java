package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.notification.service.RoommateNotificationService;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
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

import java.time.LocalDate;
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

    // --- AC-1: 1·2월 → semester=1, year=현재년도 ---

    @Test
    @DisplayName("AC-1: 1월 입력 시 semester=1 반환")
    void resolveSemester_월이_1일때_semester는_1() {
        assertThat(roommateService.resolveSemester(1)[1]).isEqualTo(1);
    }

    @Test
    @DisplayName("AC-1: 2월 입력 시 semester=1 반환")
    void resolveSemester_월이_2일때_semester는_1() {
        assertThat(roommateService.resolveSemester(2)[1]).isEqualTo(1);
    }

    @Test
    @DisplayName("AC-1: 1월 입력 시 year=현재년도 반환")
    void resolveSemester_월이_1일때_year는_현재년도() {
        assertThat(roommateService.resolveSemester(1)[0]).isEqualTo(LocalDate.now().getYear());
    }

    // --- AC-2: 7·8월 → semester=2, year=현재년도 ---

    @Test
    @DisplayName("AC-2: 7월 입력 시 semester=2 반환")
    void resolveSemester_월이_7일때_semester는_2() {
        assertThat(roommateService.resolveSemester(7)[1]).isEqualTo(2);
    }

    @Test
    @DisplayName("AC-2: 8월 입력 시 semester=2 반환")
    void resolveSemester_월이_8일때_semester는_2() {
        assertThat(roommateService.resolveSemester(8)[1]).isEqualTo(2);
    }

    @Test
    @DisplayName("AC-2: 7월 입력 시 year=현재년도 반환")
    void resolveSemester_월이_7일때_year는_현재년도() {
        assertThat(roommateService.resolveSemester(7)[0]).isEqualTo(LocalDate.now().getYear());
    }

    // --- AC-3: 그 외 월 → null ---

    @Test
    @DisplayName("AC-3: 3월 입력 시 semester=null 반환")
    void resolveSemester_월이_3일때_semester는_null() {
        assertThat(roommateService.resolveSemester(3)[1]).isNull();
    }

    @Test
    @DisplayName("AC-3: 6월 입력 시 semester=null 반환")
    void resolveSemester_월이_6일때_semester는_null() {
        assertThat(roommateService.resolveSemester(6)[1]).isNull();
    }

    @Test
    @DisplayName("AC-3: 10월 입력 시 semester=null 반환")
    void resolveSemester_월이_10일때_semester는_null() {
        assertThat(roommateService.resolveSemester(10)[1]).isNull();
    }

    @Test
    @DisplayName("AC-3: 그 외 월 입력 시 year=null 반환")
    void resolveSemester_그외월_year는_null() {
        assertThat(roommateService.resolveSemester(3)[0]).isNull();
    }

    // --- AC-4: 응답 DTO에 year·semester 포함 (entityToDto 매핑 검증) ---

    @Test
    @DisplayName("AC-4: entityToDto 변환 시 board.year가 응답에 포함")
    void entityToDto_board의_year가_응답에_포함() {
        User user = User.createForTest(1L, "테스트유저", DormType.DORM_1);
        RoommateCheckList checkList = mock(RoommateCheckList.class);
        when(checkList.getDormPeriod()).thenReturn(Collections.emptySet());
        RoommateBoard board = mock(RoommateBoard.class);
        when(board.getUser()).thenReturn(user);
        when(board.getRoommateCheckList()).thenReturn(checkList);
        when(board.getYear()).thenReturn(2026);
        when(board.getSemester()).thenReturn(1);

        ResponseRoommatePostDto result = ResponseRoommatePostDto.entityToDto(board, false, null);

        assertThat(result.getYear()).isEqualTo(2026);
    }

    @Test
    @DisplayName("AC-4: entityToDto 변환 시 board.semester가 응답에 포함")
    void entityToDto_board의_semester가_응답에_포함() {
        User user = User.createForTest(1L, "테스트유저", DormType.DORM_1);
        RoommateCheckList checkList = mock(RoommateCheckList.class);
        when(checkList.getDormPeriod()).thenReturn(Collections.emptySet());
        RoommateBoard board = mock(RoommateBoard.class);
        when(board.getUser()).thenReturn(user);
        when(board.getRoommateCheckList()).thenReturn(checkList);
        when(board.getYear()).thenReturn(2026);
        when(board.getSemester()).thenReturn(1);

        ResponseRoommatePostDto result = ResponseRoommatePostDto.entityToDto(board, false, null);

        assertThat(result.getSemester()).isEqualTo(1);
    }

    @Test
    @DisplayName("AC-4: 그 외 월 생성 게시글 entityToDto 변환 시 year=null 포함")
    void entityToDto_year가_null인_board는_응답도_null() {
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
