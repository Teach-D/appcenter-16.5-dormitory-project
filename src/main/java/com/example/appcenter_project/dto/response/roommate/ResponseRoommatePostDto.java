package com.example.appcenter_project.dto.response.roommate;

import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.*;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
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
    private int roommateBoardLike;
    private Long userId;
    private String userName;
    private LocalDateTime createdDate;

    public static ResponseRoommatePostDto entityToDto(RoommateBoard board) {
        RoommateCheckList cl = board.getRoommateCheckList();
        User user = board.getUser();

        return ResponseRoommatePostDto.builder()
                .boardId(board.getId())
                .title(cl.getTitle())
                .dormPeriod(cl.getDormPeriod())
                .dormType(cl.getDormType())
                .college(cl.getCollege())
                .religion(cl.getReligion())
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
                .roommateBoardLike(board.getRoommateBoardLike())
                .userId(user.getId())
                .userName(user.getName())
                .createdDate(board.getCreatedDate())
                .build();
    }
}

