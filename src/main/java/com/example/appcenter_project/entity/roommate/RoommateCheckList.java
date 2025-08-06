package com.example.appcenter_project.entity.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.*;
import com.example.appcenter_project.enums.user.College;
import com.example.appcenter_project.enums.user.DormType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
public class RoommateCheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 20)
    private Long id;

    @Column(length = 20)
    private String title;

    @ElementCollection(targetClass = DormDay.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "roommate_checklist_dorm_days", joinColumns = @JoinColumn(name = "roommate_checklist_id"))
    private Set<DormDay> dormPeriod;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DormType dormType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private College college;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReligionType religion;

    @Column(length = 20)
    private String mbti;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SmokingType smoking; // SMOKER, NON_SMOKER

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SnoringType snoring; // SNORER, NON_SNORER

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TeethGrindingType toothGrind; // GRINDER, NON_GRINDER

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SleepSensitivityType sleeper; // SENSITIVE_TO_LIGHT, PREFER_DARKNESS, NOT_SURE

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShowerTimeType showerHour; // MORNING, EVENING, BOTH

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShowerDurationType showerTime; // WITHIN_10_MINUTES, ...

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BedTimeType bedTime; // EARLY_SLEEPER, ...

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CleanlinessType arrangement; // NEAT, EASYGOING, UNCERTAIN

    private String comment;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    @Builder
    public RoommateCheckList(Set<DormDay> dormPeriod, DormType dormType, College college, ReligionType religion, String mbti,
                             SmokingType smoking, SnoringType snoring, TeethGrindingType toothGrind,
                             SleepSensitivityType sleeper, ShowerTimeType showerHour, ShowerDurationType showerTime,
                             BedTimeType bedTime, CleanlinessType arrangement, String comment, String title, User user) {
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
        this.title = title;
        this.user = user; // 꼭 추가
    }

    public void update(RequestRoommateFormDto dto) {
        this.title = dto.getTitle();
        this.dormPeriod = dto.getDormPeriod();
        this.dormType = dto.getDormType();
        this.college = dto.getCollege();
        this.religion = dto.getReligion();
        this.mbti = dto.getMbti();
        this.smoking = dto.getSmoking();
        this.snoring = dto.getSnoring();
        this.toothGrind = dto.getToothGrind();
        this.sleeper = dto.getSleeper();
        this.showerHour = dto.getShowerHour();
        this.showerTime = dto.getShowerTime();
        this.bedTime = dto.getBedTime();
        this.arrangement = dto.getArrangement();
        this.comment = dto.getComment();
    }


}
