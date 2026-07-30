package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoommateSemesterServiceTest {

    private final RoommateMatchingPeriodResolver periodResolver = new RoommateMatchingPeriodResolver();

    private MatchingPeriod resolve(int month) {
        return periodResolver.resolveCurrent(LocalDate.of(2026, month, 15));
    }

    // --- AC-1: 1·2월 → FIRST + OPEN ---

    @Test
    @DisplayName("AC-1: 1월 입력 시 FIRST + OPEN")
    void resolveCurrent_월이_1일때_FIRST_OPEN() {
        MatchingPeriod p = resolve(1);
        assertThat(p.semester()).isEqualTo(SemesterType.FIRST);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.OPEN);
    }

    @Test
    @DisplayName("AC-1: 2월 입력 시 FIRST + OPEN")
    void resolveCurrent_월이_2일때_FIRST_OPEN() {
        MatchingPeriod p = resolve(2);
        assertThat(p.semester()).isEqualTo(SemesterType.FIRST);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.OPEN);
    }

    // --- AC-2: 7·8월 → SECOND + OPEN ---

    @Test
    @DisplayName("AC-2: 7월 입력 시 SECOND + OPEN")
    void resolveCurrent_월이_7일때_SECOND_OPEN() {
        MatchingPeriod p = resolve(7);
        assertThat(p.semester()).isEqualTo(SemesterType.SECOND);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.OPEN);
    }

    @Test
    @DisplayName("AC-2: 8월 입력 시 SECOND + OPEN")
    void resolveCurrent_월이_8일때_SECOND_OPEN() {
        MatchingPeriod p = resolve(8);
        assertThat(p.semester()).isEqualTo(SemesterType.SECOND);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.OPEN);
    }

    // --- AC-3: 5월 → SUMMER_VACATION + OPEN ---

    @Test
    @DisplayName("AC-3: 5월 입력 시 SUMMER_VACATION + OPEN")
    void resolveCurrent_월이_5일때_SUMMER_OPEN() {
        MatchingPeriod p = resolve(5);
        assertThat(p.semester()).isEqualTo(SemesterType.SUMMER_VACATION);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.OPEN);
    }

    // --- AC-4: 11월 → WINTER_VACATION + OPEN ---

    @Test
    @DisplayName("AC-4: 11월 입력 시 WINTER_VACATION + OPEN")
    void resolveCurrent_월이_11일때_WINTER_OPEN() {
        MatchingPeriod p = resolve(11);
        assertThat(p.semester()).isEqualTo(SemesterType.WINTER_VACATION);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.OPEN);
    }

    // --- AC-5: 그 외 월 → 해당 학기 유지 + CLOSED (더 이상 null 아님) ---

    @Test
    @DisplayName("AC-5: 3월 입력 시 FIRST + CLOSED")
    void resolveCurrent_월이_3일때_FIRST_CLOSED() {
        MatchingPeriod p = resolve(3);
        assertThat(p.semester()).isEqualTo(SemesterType.FIRST);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.CLOSED);
    }

    @Test
    @DisplayName("AC-5: 6월 입력 시 SUMMER_VACATION + CLOSED")
    void resolveCurrent_월이_6일때_SUMMER_CLOSED() {
        MatchingPeriod p = resolve(6);
        assertThat(p.semester()).isEqualTo(SemesterType.SUMMER_VACATION);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.CLOSED);
    }

    @Test
    @DisplayName("AC-5: 10월 입력 시 SECOND + CLOSED")
    void resolveCurrent_월이_10일때_SECOND_CLOSED() {
        MatchingPeriod p = resolve(10);
        assertThat(p.semester()).isEqualTo(SemesterType.SECOND);
        assertThat(p.status()).isEqualTo(RoommateMatchingStatus.CLOSED);
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
    @DisplayName("AC-6: year/semester가 null인 board는 응답도 null (레거시 데이터 passthrough)")
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