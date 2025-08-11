package com.example.appcenter_project.service.calender;

import com.example.appcenter_project.dto.request.calender.RequestCalenderDto;
import com.example.appcenter_project.dto.response.calender.ResponseCalenderDto;
import com.example.appcenter_project.entity.calender.Calender;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.calender.CalenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CalenderService {

    private final CalenderRepository calenderRepository;

    public void saveCalender(RequestCalenderDto requestCalenderDto) {
        log.info("startDate : {},  endDate : {}", requestCalenderDto.getStartDate(), requestCalenderDto.getEndDate());

        Calender calender = RequestCalenderDto.dtoToEntity(requestCalenderDto);
        calenderRepository.save(calender);
    }

    public List<ResponseCalenderDto> findAllCalenders() {
        log.info("[findAllCalenders] 캘린더 전체 조회 요청");

        List<Calender> calenders = calenderRepository.findAll();
        log.info("[findAllCalenders] 조회된 캘린더 개수: {}", calenders.size());

        List<ResponseCalenderDto> responseCalenderDtos = new ArrayList<>();

        for (Calender calender : calenders) {
            ResponseCalenderDto responseCalenderDto = ResponseCalenderDto.entityToDto(calender);
            responseCalenderDtos.add(responseCalenderDto);
        }

        log.info("[findAllCalenders] 캘린더 DTO 변환 완료 - 반환할 개수: {}", responseCalenderDtos.size());

        return responseCalenderDtos;
    }

    // 특정 년월의 캘린더 조회
    public List<ResponseCalenderDto> findCalendersByYearAndMonth(int year, int month) {
        log.info("[findCalendersByYearAndMonth] {}년 {}월의 캘린더 조회 요청", year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate startOfNextMonth = yearMonth.plusMonths(1).atDay(1);

        List<Calender> calenders = calenderRepository.findByYearAndMonth(startOfMonth, startOfNextMonth);
        List<ResponseCalenderDto> responseCalenderDtos = new ArrayList<>();

        for (Calender calender : calenders) {
            ResponseCalenderDto responseCalenderDto = ResponseCalenderDto.entityToDto(calender);
            responseCalenderDtos.add(responseCalenderDto);
        }

        log.info("[findCalendersByYearAndMonth] DTO 변환 완료 - 반환할 개수: {}", responseCalenderDtos.size());

        return responseCalenderDtos;
    }

    public ResponseCalenderDto findCalender(Long calenderId) {
        log.info("[findCalender] 캘린더 상세 조회 요청 - calenderId={}", calenderId);

        Calender calender = calenderRepository.findById(calenderId).orElseThrow(() -> new CustomException(CALENDER_NOT_REGISTERED));
        log.info("[findCalender] 캘린더 조회 성공 - id={}, title={}", calender.getId(), calender.getTitle());

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
