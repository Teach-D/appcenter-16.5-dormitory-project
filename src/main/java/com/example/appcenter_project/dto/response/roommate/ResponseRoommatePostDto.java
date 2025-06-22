package com.example.appcenter_project.dto.response.roommate;

import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.enums.roommate.*;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class ResponseRoommatePostDto {
    private Long boardId;
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

    public static ResponseRoommatePostDto entityToDto(RoommateBoard board) {
        RoommateCheckList cl = board.getRoommateCheckList();

        return ResponseRoommatePostDto.builder()
                .boardId(board.getId())
                .title(cl.getTitle())
                .dormPeriod(cl.getDormPeriod())
                .dormType(cl.getDormType())
                .college(cl.getCollege())
                .mbti(cl.getMbti())
                .smoking(cl.getSmoking())
                .snoring(cl.getSnoring())
                .toothGrind(cl.getToothGrind())
                .sleeper(cl.getSleeper())
                .showerHour(cl.getShowerHour())
                .showerTime(cl.getShowerTime())
                .bedTime(cl.getBedTime())
                .arrangement(cl.getArrangement())
                .comment(cl.getComment())
                .build();
    }
}

