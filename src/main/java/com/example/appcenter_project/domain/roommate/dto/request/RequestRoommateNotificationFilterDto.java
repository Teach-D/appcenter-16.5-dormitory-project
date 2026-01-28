package com.example.appcenter_project.domain.roommate.dto.request;

import com.example.appcenter_project.domain.roommate.enums.*;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import lombok.Getter;

import java.util.Set;

@Getter
public class RequestRoommateNotificationFilterDto {
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
}

