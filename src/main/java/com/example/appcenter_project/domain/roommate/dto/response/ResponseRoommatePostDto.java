package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.enums.*;
import com.example.appcenter_project.domain.user.dto.response.ResponseBoardDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.shared.util.DormDayUtil;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ResponseRoommatePostDto extends ResponseBoardDto {
    private List<DormDay> dormPeriod; // Set → List로 타입 변경
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
    private String userProfileImageUrl;

    @Builder
    public ResponseRoommatePostDto(Long id, String title, String type, LocalDateTime createDate, String filePath,
                                   List<DormDay> dormPeriod, DormType dormType, College college, ReligionType religion,
                                   String mbti, SmokingType smoking, SnoringType snoring, TeethGrindingType toothGrind,
                                   SleepSensitivityType sleeper, ShowerTimeType showerHour, ShowerDurationType showerTime,
                                   BedTimeType bedTime, CleanlinessType arrangement, String comment,
                                   int roommateBoardLike, Long userId, String userName, boolean isMatched,
                                   String userProfileImageUrl) {
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
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public static ResponseRoommatePostDto entityToDto(RoommateBoard board, boolean isMatched, String userProfileImageUrl) {
        RoommateCheckList cl = board.getRoommateCheckList();
        User user = board.getUser();

        return ResponseRoommatePostDto.builder()
                .id(board.getId())
                .title(cl.getTitle())
                .type("ROOMMATE")
                .createDate(board.getCreatedDate())
                .filePath(null) // 필요시 이미지 경로 설정
                .dormPeriod(DormDayUtil.sortDormDays(cl.getDormPeriod()))
                .dormType(user.getDormType())
                .college(user.getCollege())
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
                .userProfileImageUrl(userProfileImageUrl)
                .build();
    }
}
