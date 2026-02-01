package com.example.appcenter_project.domain.roommate.entity;

import com.example.appcenter_project.global.converter.StringListConverter;
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
    @JoinColumn(name = "member_id", unique = true, nullable = true)
    private User user;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "roommate_id", unique = true)
    private User roommate;

    @Builder
    public MyRoommate(List<String> rule, User roommate, User user) {
        this.rule = rule;
        this.roommate = roommate;
        this.user = user;
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
