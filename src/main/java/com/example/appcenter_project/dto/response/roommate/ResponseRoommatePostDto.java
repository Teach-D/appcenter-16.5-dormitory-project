package com.example.appcenter_project.dto.response.roommate;

import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
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
public class ResponseRoommatePostDto extends ResponseBoardDto {
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
    private boolean isMatched;

    @Builder
    public ResponseRoommatePostDto(Long id, String title, String type, LocalDateTime createDate, String filePath,
                                   Set<DormDay> dormPeriod, DormType dormType, College college, ReligionType religion,
                                   String mbti, SmokingType smoking, SnoringType snoring, TeethGrindingType toothGrind,
                                   SleepSensitivityType sleeper, ShowerTimeType showerHour, ShowerDurationType showerTime,
                                   BedTimeType bedTime, CleanlinessType arrangement, String comment,
                                   int roommateBoardLike, Long userId, String userName, boolean isMatched) {
        super(id, title, type, createDate, filePath);
        this.dormPeriod = dormPeriod;
        this.dormType = dormType;
        this.college = college;
        this.religion = religion;
        this.mbti = mbti;
        this.smoking = smoking;
        this.snoring = snoring;
        this.toothGrind = toothGrind;
        this.sleeper = sleeper;
        this.showerHour = showerHour;
        this.showerTime = showerTime;
        this.bedTime = bedTime;
        this.arrangement = arrangement;
        this.comment = comment;
        this.roommateBoardLike = roommateBoardLike;
        this.userId = userId;
        this.userName = userName;
        this.isMatched = isMatched;
    }

    public static ResponseRoommatePostDto entityToDto(RoommateBoard board, boolean isMatched) {
        RoommateCheckList cl = board.getRoommateCheckList();
        User user = board.getUser();

        return ResponseRoommatePostDto.builder()
                .id(board.getId())
                .title(cl.getTitle())
                .type("ROOMMATE")
                .createDate(board.getCreatedDate())
                .filePath(null) // 필요시 이미지 경로 설정
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
                .isMatched(isMatched)
                .build();
    }
}