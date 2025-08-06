package com.example.appcenter_project.dto.response.roommate;

import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.enums.roommate.*;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class ResponseRoommateCheckListDto {

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

    public static ResponseRoommateCheckListDto from(RoommateCheckList checklist) {
        return ResponseRoommateCheckListDto.builder()
                .title(checklist.getTitle())
                .dormPeriod(checklist.getDormPeriod())
                .dormType(checklist.getDormType())
                .college(checklist.getCollege())
                .mbti(checklist.getMbti())
                .smoking(checklist.getSmoking())
                .snoring(checklist.getSnoring())
                .toothGrind(checklist.getToothGrind())
                .sleeper(checklist.getSleeper())
                .showerHour(checklist.getShowerHour())
                .showerTime(checklist.getShowerTime())
                .bedTime(checklist.getBedTime())
                .arrangement(checklist.getArrangement())
                .comment(checklist.getComment())
                .build();
    }
}
