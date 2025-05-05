package com.example.appcenter_project.entity.roommate;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.like.RoommateBoardLike;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoommateBoard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "roommate_checklist_id", nullable = false)
    private RoommateCheckList roommateCheckList;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer roommateBoardLike;

    private String title;

    @OneToMany(mappedBy = "roommateBoard")
    private List<RoommateBoardLike> roommateBoardLikeList = new ArrayList<>();

    private int likeCount;

    public int plusLike() {
        this.likeCount += 1;
        return this.likeCount;
    }

    @Builder
    public RoommateBoard(RoommateCheckList roommateCheckList, User user) {
        this.roommateCheckList = roommateCheckList;
        this.user = user;
        this.roommateBoardLike = 0;
    }

}
