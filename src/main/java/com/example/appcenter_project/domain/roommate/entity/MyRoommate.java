package com.example.appcenter_project.domain.roommate.entity;

import com.example.appcenter_project.global.converter.StringListConverter;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.enums.SemesterTypeConverter;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MyRoommate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = StringListConverter.class)
    private List<String> rule = new ArrayList<>();

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private User user;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "roommate_id", unique = true)
    private User roommate;

    @Column(name = "registration_year")
    private Integer year;

    @Convert(converter = SemesterTypeConverter.class)
    private SemesterType semester;

    @Builder
    public MyRoommate(List<String> rule, User roommate, User user, Integer year, SemesterType semester) {
        this.rule = rule;
        this.roommate = roommate;
        this.user = user;
        this.year = year;
        this.semester = semester;
    }

    public void updateRules(List<String> newRules) {
        this.rule.addAll(newRules);
    }

    public void initRule() {
        if (this.rule == null) {
            this.rule = new ArrayList<>();
        }
    }

}
