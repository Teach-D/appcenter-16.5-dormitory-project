package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.entity.RoommateNotificationFilter;
import com.example.appcenter_project.domain.roommate.enums.*;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class ResponseRoommateNotificationFilterDto {
    // 기본 정보
    private DormType dormType; // 기숙사 종류
    private Set<DormDay> dormPeriodDays; // 기숙사 비상주기간
    private Set<College> colleges; // 단과대

    // 생활 습관
    private SmokingType smoking; // 흡연여부
    private SnoringType snoring; // 코골이유무
    private TeethGrindingType toothGrind; // 이갈이유무
    private SleepSensitivityType sleeper; // 잠귀

    // 생활 리듬
    private ShowerTimeType showerHour; // 샤워 시기
    private ShowerDurationType showerTime; // 샤워 시간
    private BedTimeType bedTime; // 취침시기

    // 성향
    private CleanlinessType arrangement; // 정리정돈
    private Set<ReligionType> religions; // 종교

    public static ResponseRoommateNotificationFilterDto from(RoommateNotificationFilter filter) {
        if (filter == null) {
            // 필터가 없으면 모든 필드가 null인 빈 객체 반환
            return ResponseRoommateNotificationFilterDto.builder()
                    .dormType(null)
                    .dormPeriodDays(null)
                    .colleges(null)
                    .smoking(null)
                    .snoring(null)
                    .toothGrind(null)
                    .sleeper(null)
                    .showerHour(null)
                    .showerTime(null)
                    .bedTime(null)
                    .arrangement(null)
                    .religions(null)
                    .build();
        }

        return ResponseRoommateNotificationFilterDto.builder()
                .dormType(filter.getDormType())
                .dormPeriodDays(filter.getDormPeriodDays())
                .colleges(filter.getColleges())
                .smoking(filter.getSmoking())
                .snoring(filter.getSnoring())
                .toothGrind(filter.getToothGrind())
                .sleeper(filter.getSleeper())
                .showerHour(filter.getShowerHour())
                .showerTime(filter.getShowerTime())
                .bedTime(filter.getBedTime())
                .arrangement(filter.getArrangement())
                .religions(filter.getReligions())
                .build();
    }
}

