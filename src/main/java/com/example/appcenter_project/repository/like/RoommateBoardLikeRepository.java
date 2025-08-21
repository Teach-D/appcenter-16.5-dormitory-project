package com.example.appcenter_project.repository.like;

import com.example.appcenter_project.entity.like.RoommateBoardLike;
import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoommateBoardLikeRepository extends JpaRepository<RoommateBoardLike, Long> {
    boolean existsByUserAndRoommateBoard(User user, RoommateBoard roommateBoard);
    Optional<RoommateBoardLike> findByUserAndRoommateBoard(User user, RoommateBoard roommateBoard);

    List<RoommateBoardLike> findByUserId(Long userId);

    @Query("SELECT DISTINCT rbl FROM RoommateBoardLike rbl " +
            "JOIN FETCH rbl.roommateBoard rb " +
            "LEFT JOIN FETCH rb.roommateBoardLikeList " +
            "JOIN FETCH rb.roommateCheckList rcl " +
            "LEFT JOIN FETCH rcl.dormPeriod " +
            "JOIN FETCH rcl.user u " +
            "LEFT JOIN FETCH u.roommateBoard " +
            "WHERE rbl.user.id = :userId")
    List<RoommateBoardLike> findByUserIdWithRoommateBoadAndRoommateCheckListAndUser(@Param("userId") Long userId);
}
