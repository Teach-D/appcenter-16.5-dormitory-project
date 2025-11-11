package com.example.appcenter_project.shared.utils;

import org.springframework.stereotype.Component;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class WorkingHoursValidator {

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    /**
     * 현재 시간이 평일 근무시간인지 확인
     * @return 평일 09:00~18:00 사이면 true, 아니면 false
     */
    public boolean isWorkingHours() {
        return isWorkingHours(LocalDateTime.now());
    }

    public boolean isNotWorkingHours() {
        return !isWorkingHours();
    }

    /**
     * 특정 시간이 평일 근무시간인지 확인
     * @param dateTime 확인할 날짜/시간
     * @return 평일 09:00~18:00 사이면 true, 아니면 false
     */
    public boolean isWorkingHours(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        // 주말 체크 (토요일, 일요일)
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        // 시간 체크 (09:00 이상, 18:00 미만)
        return !time.isBefore(WORK_START) && time.isBefore(WORK_END);
    }
}