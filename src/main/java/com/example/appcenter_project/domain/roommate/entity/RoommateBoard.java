package com.example.appcenter_project.domain.roommate.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
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
public class RoommateBoard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "roommate_checklist_id")
    private RoommateCheckList roommateCheckList;

    @OneToMany(mappedBy = "roommateBoard", orphanRemoval = true)
    private List<RoommateBoardLike> roommateBoardLikeList = new ArrayList<>();

    @Column(nullable = false)
    private int roommateBoardLike = 0;

    private boolean isMatched = false;

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

    public void updateTitle(String title){
        this.title = title;
    }

    public void changeIsMatched(boolean isMatched){
        this.isMatched = isMatched;
    }
}