package com.example.appcenter_project.entity.like;

import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class RoommateBoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "roommate_board_id")
    private RoommateBoard roommateBoard;

    @Builder
    public RoommateBoardLike(User user, RoommateBoard roommateBoard) {
        this.user = user;
        this.roommateBoard = roommateBoard;
    }
}