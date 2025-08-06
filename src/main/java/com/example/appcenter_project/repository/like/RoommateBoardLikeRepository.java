package com.example.appcenter_project.repository.like;

import com.example.appcenter_project.entity.like.RoommateBoardLike;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoommateBoardLikeRepository extends JpaRepository<RoommateBoardLike, Long> {
    boolean existsByUserAndRoommateBoard(User user, RoommateBoard roommateBoard);
    Optional<RoommateBoardLike> findByUserAndRoommateBoard(User user, RoommateBoard roommateBoard);

    List<RoommateBoardLike> findByUserId(Long userId);
}
