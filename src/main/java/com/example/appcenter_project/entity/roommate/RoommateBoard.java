package com.example.appcenter_project.entity.roommate;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.reflect.Member;

@Entity
@Getter
@NoArgsConstructor
public class RoommateBoard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "roommate_checklist_id")
    private RoommateCheckList roommateCheckList;

    private int roommateBoardLike;

    public Integer plusLike(){
        this.roommateBoardLike +=1;
        return this.getRoommateBoardLike();
    }

    public Integer minusLike(){
        this.roommateBoardLike -=1;
        return this.getRoommateBoardLike();
    }

    @Builder
    public RoommateBoard(String title, User user, int roommateBoardLike, RoommateCheckList roommateCheckList) {
        this.title = title;
        this.user = user;
        this.roommateBoardLike = roommateBoardLike;
        this.roommateCheckList = roommateCheckList;
    }

    public void setRoommateCheckList(RoommateCheckList roommateCheckList) {
        this.roommateCheckList = roommateCheckList;
    }

    public void setUser(User user) {
        this.user = user;
    }



}