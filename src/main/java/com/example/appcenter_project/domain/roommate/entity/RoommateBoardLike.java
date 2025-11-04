package com.example.appcenter_project.domain.roommate.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class RoommateBoardLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 좋아요 누른 룸메이트 게시글
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "roommate_board_id")
    private RoommateBoard roommateBoard;

    @Builder
    public RoommateBoardLike(User user, RoommateBoard roommateBoard) {
        this.user = user;
        this.roommateBoard = roommateBoard;
    }
}
