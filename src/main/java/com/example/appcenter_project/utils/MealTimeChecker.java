package com.example.appcenter_project.utils;

import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class MealTimeChecker {

    // 점심시간: 12:00 ~ 14:00
    private static final int LUNCH_START_HOUR = 12;
    private static final int LUNCH_END_HOUR = 14;

    // 저녁시간: 18:00 ~ 20:00
    private static final int DINNER_START_HOUR = 18;
    private static final int DINNER_END_HOUR = 20;

    /**
     * 현재가 점심 또는 저녁 시간인지 확인하는 메서드
     */
    public boolean isMealTime() {
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();

        return isLunchTime(currentHour) || isDinnerTime(currentHour);
    }

    public boolean isLunchTime() {
        return isLunchTime(LocalTime.now().getHour());
    }

    public boolean isDinnerTime() {
        return isDinnerTime(LocalTime.now().getHour());
    }

    private boolean isLunchTime(int hour) {
        return hour >= LUNCH_START_HOUR && hour < LUNCH_END_HOUR;
    }

    private boolean isDinnerTime(int hour) {
        return hour >= DINNER_START_HOUR && hour < DINNER_END_HOUR;
    }

    public String getCurrentMealType() {
        if (isLunchTime()) return "LUNCH";
        if (isDinnerTime()) return "DINNER";
        return "NORMAL";
    }
}