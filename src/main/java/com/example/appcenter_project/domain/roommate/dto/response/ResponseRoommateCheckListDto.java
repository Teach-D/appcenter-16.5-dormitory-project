package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.enums.*;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
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

    public static ResponseRoommateCheckListDto from(RoommateCheckList checklist) {
        User user = checklist.getUser();

        return ResponseRoommateCheckListDto.builder()
                .title(checklist.getTitle())
                .dormPeriod(checklist.getDormPeriod())
                .dormType(user.getDormType())
                .college(user.getCollege())
                .religion(checklist.getReligion())
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
