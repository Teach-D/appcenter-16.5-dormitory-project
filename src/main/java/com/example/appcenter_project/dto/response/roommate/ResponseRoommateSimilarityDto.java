package com.example.appcenter_project.dto.response.roommate;

import com.example.appcenter_project.enums.roommate.*;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ResponseRoommateSimilarityDto {
    private Long boardId;
    private String title;
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
    private Long userId;
    private String userName;
    private LocalDateTime createdDate;
    private boolean isMatched;
    private String userProfileImageUrl;

    private int roommateBoardLike;
    private Integer similarityPercentage;
}
