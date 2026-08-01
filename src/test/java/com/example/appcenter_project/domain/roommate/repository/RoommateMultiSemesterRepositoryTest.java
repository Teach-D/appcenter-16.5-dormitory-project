package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RoommateMultiSemesterRepositoryTest {

    @Mock
    RoommateCheckListRepository roommateCheckListRepository;

    @Mock
    RoommateBoardRepository roommateBoardRepository;

    @Mock
    MyRoommateRepository myRoommateRepository;

    @Test
    @DisplayName("true 반환 — AC-2 BR-710 existsByUserIdAndYearAndSemester — 동일 학기 체크리스트 존재 시 true")
    void should_return_true_when_checklist_exists_for_same_semester() {
        // given
        given(roommateCheckListRepository.existsByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST))
                .willReturn(true);

        // when
        boolean result = roommateCheckListRepository.existsByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("false 반환 — AC-3 BR-710 existsByUserIdAndYearAndSemester — 다른 학기 체크리스트는 false")
    void should_return_false_when_checklist_exists_only_in_different_semester() {
        // given
        given(roommateCheckListRepository.existsByUserIdAndYearAndSemester(1L, 2026, SemesterType.SECOND))
                .willReturn(false);

        // when
        boolean result = roommateCheckListRepository.existsByUserIdAndYearAndSemester(1L, 2026, SemesterType.SECOND);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Optional.empty 반환 — AC-8 BR-710 findByUserIdAndYearAndSemester — 해당 학기 게시글 없을 때")
    void should_return_empty_when_board_not_found_for_current_semester() {
        // given
        given(roommateBoardRepository.findByUserIdAndYearAndSemester(99L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());

        // when
        Optional<?> result = roommateBoardRepository.findByUserIdAndYearAndSemester(99L, 2026, SemesterType.FIRST);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Optional.empty 반환 — AC-13 BR-710 findByUserIdAndRoommateIdAndYearAndSemester — 현재 학기 매칭 없을 때")
    void should_return_empty_when_myRoommate_not_found_for_current_semester() {
        // given
        given(myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());

        // when
        Optional<?> result = myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("1 반환 — AC-14 BR-710 deleteByUserIdAndRoommateIdAndYearAndSemester — 현재 학기 행 삭제")
    void should_delete_myRoommate_row_for_current_semester_only() {
        // given
        given(myRoommateRepository.deleteByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST))
                .willReturn(1);

        // when
        int deleted = myRoommateRepository.deleteByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST);

        // then
        assertThat(deleted).isEqualTo(1);
    }
}
