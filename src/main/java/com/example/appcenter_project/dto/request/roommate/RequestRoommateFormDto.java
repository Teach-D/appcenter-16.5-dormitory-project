package com.example.appcenter_project.dto.request.roommate;

import com.example.appcenter_project.enums.roommate.*;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import lombok.Getter;

import java.util.Set;

@Getter
public class RequestRoommateFormDto {
    private String title;
    private Set<DormDay> dormPeriod;
    private DormType dormType;
    private College college;
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
