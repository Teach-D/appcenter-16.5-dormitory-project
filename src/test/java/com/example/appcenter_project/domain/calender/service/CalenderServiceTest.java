package com.example.appcenter_project.domain.calender.service;

import com.example.appcenter_project.domain.calender.dto.request.RequestCalenderDto;
import com.example.appcenter_project.domain.calender.dto.response.ResponseCalenderDto;
import com.example.appcenter_project.domain.calender.entity.Calender;
import com.example.appcenter_project.domain.calender.repository.CalenderRepository;
import com.example.appcenter_project.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.CALENDER_NOT_REGISTERED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalenderServiceTest {

    @Mock
    CalenderRepository calenderRepository;

    @InjectMocks
    CalenderService calenderService;

    @Test
    @DisplayName("캘린더 저장 - 정상 저장")
    void saveCalender_정상_저장() {
        RequestCalenderDto dto = mock(RequestCalenderDto.class);
        when(dto.getStartDate()).thenReturn(LocalDate.of(2025, 8, 1));
        when(dto.getEndDate()).thenReturn(LocalDate.of(2025, 8, 31));
        when(dto.getTitle()).thenReturn("중간고사");
        when(dto.getLink()).thenReturn("https://example.com");

        calenderService.saveCalender(dto);

        verify(calenderRepository).save(any(Calender.class));
    }

    @Test
    @DisplayName("캘린더 전체 조회 - 정상 반환")
    void findAllCalenders_정상_조회() {
        Calender mockCalender = mock(Calender.class);
        when(mockCalender.getId()).thenReturn(1L);
        when(mockCalender.getTitle()).thenReturn("중간고사");
        when(mockCalender.getStartDate()).thenReturn(LocalDate.of(2025, 8, 1));
        when(mockCalender.getEndDate()).thenReturn(LocalDate.of(2025, 8, 31));
        when(calenderRepository.findAll()).thenReturn(List.of(mockCalender));

        List<ResponseCalenderDto> result = calenderService.findAllCalenders();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("중간고사");
    }

    @Test
    @DisplayName("캘린더 전체 조회 - 빈 목록이면 빈 리스트 반환")
    void findAllCalenders_빈목록_빈리스트() {
        when(calenderRepository.findAll()).thenReturn(List.of());

        List<ResponseCalenderDto> result = calenderService.findAllCalenders();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("캘린더 월별 조회 - 정상 반환")
    void findCalendersByYearAndMonth_정상_조회() {
        Calender mockCalender = mock(Calender.class);
        when(mockCalender.getId()).thenReturn(1L);
        when(mockCalender.getTitle()).thenReturn("중간고사");
        when(mockCalender.getStartDate()).thenReturn(LocalDate.of(2025, 8, 5));
        when(mockCalender.getEndDate()).thenReturn(LocalDate.of(2025, 8, 10));
        when(calenderRepository.findByMonthBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(mockCalender));

        List<ResponseCalenderDto> result = calenderService.findCalendersByYearAndMonth(2025, 8);

        assertThat(result).hasSize(1);
        verify(calenderRepository).findByMonthBetween(
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 9, 1)
        );
    }

    @Test
    @DisplayName("캘린더 월별 조회 - 빈 목록이면 빈 리스트 반환")
    void findCalendersByYearAndMonth_빈목록_빈리스트() {
        when(calenderRepository.findByMonthBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        List<ResponseCalenderDto> result = calenderService.findCalendersByYearAndMonth(2025, 8);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("캘린더 단일 조회 - 정상 반환")
    void findCalender_정상_반환() {
        Calender mockCalender = mock(Calender.class);
        when(mockCalender.getId()).thenReturn(1L);
        when(mockCalender.getTitle()).thenReturn("중간고사");
        when(mockCalender.getStartDate()).thenReturn(LocalDate.of(2025, 8, 1));
        when(mockCalender.getEndDate()).thenReturn(LocalDate.of(2025, 8, 31));
        when(calenderRepository.findById(1L)).thenReturn(Optional.of(mockCalender));

        ResponseCalenderDto result = calenderService.findCalender(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("중간고사");
    }

    @Test
    @DisplayName("캘린더 단일 조회 - 없으면 예외")
    void findCalender_없으면_예외() {
        when(calenderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calenderService.findCalender(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CALENDER_NOT_REGISTERED);
    }

    @Test
    @DisplayName("캘린더 수정 - 정상 수정")
    void updateCalender_정상_수정() {
        Calender mockCalender = mock(Calender.class);
        when(calenderRepository.findById(1L)).thenReturn(Optional.of(mockCalender));

        RequestCalenderDto dto = mock(RequestCalenderDto.class);

        calenderService.updateCalender(1L, dto);

        verify(mockCalender).update(dto);
    }

    @Test
    @DisplayName("캘린더 수정 - 없으면 예외")
    void updateCalender_없으면_예외() {
        when(calenderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calenderService.updateCalender(99L, mock(RequestCalenderDto.class)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CALENDER_NOT_REGISTERED);
    }

    @Test
    @DisplayName("캘린더 삭제 - 정상 삭제")
    void deleteCalender_정상_삭제() {
        Calender mockCalender = mock(Calender.class);
        when(calenderRepository.findById(1L)).thenReturn(Optional.of(mockCalender));

        calenderService.deleteCalender(1L);

        verify(calenderRepository).deleteById(1L);
    }

    @Test
    @DisplayName("캘린더 삭제 - 없으면 예외")
    void deleteCalender_없으면_예외() {
        when(calenderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> calenderService.deleteCalender(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", CALENDER_NOT_REGISTERED);
    }
}
