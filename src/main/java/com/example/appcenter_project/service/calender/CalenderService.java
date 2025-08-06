package com.example.appcenter_project.service.calender;

import com.example.appcenter_project.dto.request.calender.RequestCalenderDto;
import com.example.appcenter_project.dto.response.calender.ResponseCalenderDto;
import com.example.appcenter_project.entity.calender.Calender;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.calender.CalenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.CALENDER_NOT_REGISTERED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CalenderService {

    private final CalenderRepository calenderRepository;

    public void saveCalender(RequestCalenderDto requestCalenderDto) {
        Calender calender = RequestCalenderDto.dtoToEntity(requestCalenderDto);
        calenderRepository.save(calender);
    }

    public List<ResponseCalenderDto> findAllCalenders() {
        List<Calender> calenders = calenderRepository.findAll();
        List<ResponseCalenderDto> responseCalenderDtos = new ArrayList<>();

        for (Calender calender : calenders) {
            ResponseCalenderDto responseCalenderDto = ResponseCalenderDto.entityToDto(calender);
            responseCalenderDtos.add(responseCalenderDto);
        }

        return responseCalenderDtos;
    }

    // 특정 년월의 캘린더 조회
    public List<ResponseCalenderDto> findCalendersByYearAndMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate startOfNextMonth = yearMonth.plusMonths(1).atDay(1);
        
        List<Calender> calenders = calenderRepository.findByYearAndMonth(startOfMonth, startOfNextMonth);
        List<ResponseCalenderDto> responseCalenderDtos = new ArrayList<>();

        for (Calender calender : calenders) {
            ResponseCalenderDto responseCalenderDto = ResponseCalenderDto.entityToDto(calender);
            responseCalenderDtos.add(responseCalenderDto);
        }

        return responseCalenderDtos;
    }

    public ResponseCalenderDto findCalender(Long calenderId) {
        Calender calender = calenderRepository.findById(calenderId).orElseThrow(() -> new CustomException(CALENDER_NOT_REGISTERED));
        return ResponseCalenderDto.entityToDto(calender);
    }

    public void updateCalender(Long calenderId, RequestCalenderDto requestCalenderDto) {
        Calender calender = calenderRepository.findById(calenderId).orElseThrow(() -> new CustomException(CALENDER_NOT_REGISTERED));
        calender.update(requestCalenderDto);
    }

    public void deleteCalender(Long calenderId) {
        calenderRepository.deleteById(calenderId);
    }
}
