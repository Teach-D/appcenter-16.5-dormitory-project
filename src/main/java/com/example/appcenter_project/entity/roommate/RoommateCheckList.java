package com.example.appcenter_project.entity.roommate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoommateCheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(length = 30)
    private String comment;

    @Builder
    public RoommateCheckList(String dormPeriod, String dormType, String college, String mbit) {
        this.dormPeriod = dormPeriod;
        this.dormType = dormType;
        this.college = college;
        this.mbit = mbit;
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }
}
