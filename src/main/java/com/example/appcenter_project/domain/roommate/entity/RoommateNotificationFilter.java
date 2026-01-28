package com.example.appcenter_project.domain.roommate.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.roommate.enums.*;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.College;
import com.example.appcenter_project.domain.user.enums.DormType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "roommate_notification_filter")
public class RoommateNotificationFilter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // 기본 정보
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DormType dormType; // 기숙사 종류

    @ElementCollection(targetClass = DormDay.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "roommate_notification_filter_dorm_days", 
                    joinColumns = @JoinColumn(name = "roommate_notification_filter_id"))
    private Set<DormDay> dormPeriodDays; // 기숙사 비상주기간

    @ElementCollection(targetClass = College.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "roommate_notification_filter_colleges",
                    joinColumns = @JoinColumn(name = "roommate_notification_filter_id"))
    private Set<College> colleges; // 단과대

    // 생활 습관
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SmokingType smoking; // 흡연여부

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SnoringType snoring; // 코골이유무

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TeethGrindingType toothGrind; // 이갈이유무

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SleepSensitivityType sleeper; // 잠귀

    // 생활 리듬
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShowerTimeType showerHour; // 샤워 시기

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ShowerDurationType showerTime; // 샤워 시간

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BedTimeType bedTime; // 취침시기

    // 성향
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CleanlinessType arrangement; // 정리정돈

    @ElementCollection(targetClass = ReligionType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "roommate_notification_filter_religions",
                    joinColumns = @JoinColumn(name = "roommate_notification_filter_id"))
    private Set<ReligionType> religions; // 종교

    @Builder
    public RoommateNotificationFilter(User user, DormType dormType, Set<DormDay> dormPeriodDays,
                                      Set<College> colleges, SmokingType smoking, SnoringType snoring,
                                      TeethGrindingType toothGrind, SleepSensitivityType sleeper,
                                      ShowerTimeType showerHour, ShowerDurationType showerTime,
                                      BedTimeType bedTime, CleanlinessType arrangement,
                                      Set<ReligionType> religions) {
        this.user = user;
        this.dormType = dormType;
        this.dormPeriodDays = dormPeriodDays;
        this.colleges = colleges;
        this.smoking = smoking;
        this.snoring = snoring;
        this.toothGrind = toothGrind;
        this.sleeper = sleeper;
        this.showerHour = showerHour;
        this.showerTime = showerTime;
        this.bedTime = bedTime;
        this.arrangement = arrangement;
        this.religions = religions;
    }

    public void update(DormType dormType, Set<DormDay> dormPeriodDays,
                      Set<College> colleges, SmokingType smoking, SnoringType snoring,
                      TeethGrindingType toothGrind, SleepSensitivityType sleeper,
                      ShowerTimeType showerHour, ShowerDurationType showerTime,
                      BedTimeType bedTime, CleanlinessType arrangement,
                      Set<ReligionType> religions) {
        this.dormType = dormType;
        this.dormPeriodDays = dormPeriodDays;
        this.colleges = colleges;
        this.smoking = smoking;
        this.snoring = snoring;
        this.toothGrind = toothGrind;
        this.sleeper = sleeper;
        this.showerHour = showerHour;
        this.showerTime = showerTime;
        this.bedTime = bedTime;
        this.arrangement = arrangement;
        this.religions = religions;
    }
}

