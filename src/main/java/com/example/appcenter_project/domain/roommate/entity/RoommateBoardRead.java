package com.example.appcenter_project.domain.roommate.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "roommate_board_read",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_roommate_board_read_user_board",
                columnNames = {"user_id", "roommate_board_id"}
        )
)
public class RoommateBoardRead extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roommate_board_id")
    private RoommateBoard roommateBoard;

    private RoommateBoardRead(User user, RoommateBoard roommateBoard) {
        this.user = user;
        this.roommateBoard = roommateBoard;
    }

    public static RoommateBoardRead of(User user, RoommateBoard roommateBoard) {
        return new RoommateBoardRead(user, roommateBoard);
    }
}