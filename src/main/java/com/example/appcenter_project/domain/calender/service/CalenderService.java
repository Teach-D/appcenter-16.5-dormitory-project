package com.example.appcenter_project.domain.calender.service;

import com.example.appcenter_project.domain.calender.dto.request.RequestCalenderDto;
import com.example.appcenter_project.domain.calender.dto.response.ResponseCalenderDto;
import com.example.appcenter_project.domain.calender.entity.Calender;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.calender.repository.CalenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CalenderService {

    private final CalenderRepository calenderRepository;

    public void saveCalender(RequestCalenderDto requestDto) {
        Calender calender = Calender.from(requestDto);
        calenderRepository.save(calender);
    }

    public List<ResponseCalenderDto> findAllCalenders() {
        List<Calender> calenders = calenderRepository.findAll();
        return calenders.stream()
                .map(ResponseCalenderDto::from)
                .toList();
    }

    public List<ResponseCalenderDto> findCalendersByYearAndMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate startOfNextMonth = yearMonth.plusMonths(1).atDay(1);

        List<Calender> calenders = calenderRepository.findByMonthBetween(startOfMonth, startOfNextMonth);
        return calenders.stream()
                .map(ResponseCalenderDto::from)
                .toList();
    }

    public ResponseCalenderDto findCalender(Long calenderId) {
        Calender calender = calenderRepository.findById(calenderId).orElseThrow(() -> new CustomException(CALENDER_NOT_REGISTERED));
        return ResponseCalenderDto.from(calender);
    }

    public void updateCalender(Long calenderId, RequestCalenderDto requestDto) {
        Calender calender = calenderRepository.findById(calenderId).orElseThrow(() -> new CustomException(CALENDER_NOT_REGISTERED));
        calender.update(requestDto);
    }

    public void deleteCalender(Long calenderId) {
        Calender calender = calenderRepository.findById(calenderId).orElseThrow(() -> new CustomException(CALENDER_NOT_REGISTERED));
        calenderRepository.deleteById(calenderId);
    }
}
