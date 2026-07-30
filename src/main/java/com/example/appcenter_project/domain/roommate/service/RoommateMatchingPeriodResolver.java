package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RoommateMatchingPeriodResolver {

    public MatchingPeriod resolveCurrent(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();

        return switch (month) {
            case 1, 2  -> new MatchingPeriod(year, SemesterType.FIRST, RoommateMatchingStatus.OPEN);
            case 3, 4  -> new MatchingPeriod(year, SemesterType.FIRST, RoommateMatchingStatus.CLOSED);
            case 5     -> new MatchingPeriod(year, SemesterType.SUMMER_VACATION, RoommateMatchingStatus.OPEN);
            case 6     -> new MatchingPeriod(year, SemesterType.SUMMER_VACATION, RoommateMatchingStatus.CLOSED);
            case 7, 8  -> new MatchingPeriod(year, SemesterType.SECOND, RoommateMatchingStatus.OPEN);
            case 9, 10 -> new MatchingPeriod(year, SemesterType.SECOND, RoommateMatchingStatus.CLOSED);
            case 11    -> new MatchingPeriod(year, SemesterType.WINTER_VACATION, RoommateMatchingStatus.OPEN);
            default    -> new MatchingPeriod(year, SemesterType.WINTER_VACATION, RoommateMatchingStatus.CLOSED); // 12월
        };
    }
}