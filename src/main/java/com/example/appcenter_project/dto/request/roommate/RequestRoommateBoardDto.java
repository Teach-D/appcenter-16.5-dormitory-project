package com.example.appcenter_project.dto.request.roommate;

import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import lombok.Getter;

@Getter
public class RequestRoommateBoardDto {

    private String title;

    private String dormPeriod;
    private String dormType;
    private String college;
    private String mbit;
    private String smoking;
    private String snoring;
    private String toothGrind;
    private String sleeper;
    private String showerHour;
    private String showerTime;
    private String bedTime;
    private String arrangement;
    private String comment;

    //요청 DTO를 체크리스트 엔티티로 변환
    public static RoommateCheckList dtoToCheckList(RequestRoommateBoardDto dto) {
        return RoommateCheckList.builder()
                .dormPeriod(dto.getDormPeriod())
                .dormType(dto.getDormType())
                .college(dto.getCollege())
                .mbit(dto.getMbit())
                .smoking(dto.getSmoking())
                .snoring(dto.getSnoring())
                .toothGrind(dto.getToothGrind())
                .sleeper(dto.getSleeper())
                .showerHour(dto.getShowerHour())
                .showerTime(dto.getShowerTime())
                .bedTime(dto.getBedTime())
                .arrangement(dto.getArrangement())
                .comment(dto.getComment())
                .build();
    }
}

