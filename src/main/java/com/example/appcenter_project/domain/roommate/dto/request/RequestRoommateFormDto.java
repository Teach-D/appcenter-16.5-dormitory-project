package com.example.appcenter_project.domain.roommate.dto.request;

import com.example.appcenter_project.domain.roommate.enums.*;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import lombok.Getter;

import java.util.Set;

@Getter
public class RequestRoommateFormDto {
    private String title;
    private Set<DormDay> dormPeriod;
    private DormType dormType;
    private College college;
    private ReligionType religion;
    private String mbti;
    private SmokingType smoking;
    private SnoringType snoring;
    private TeethGrindingType toothGrind;
    private SleepSensitivityType sleeper;
    private ShowerTimeType showerHour;
    private ShowerDurationType showerTime;
    private BedTimeType bedTime;
    private CleanlinessType arrangement;
    private String comment;
}
