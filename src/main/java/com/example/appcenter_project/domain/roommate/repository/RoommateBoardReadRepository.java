package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateBoardRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RoommateBoardReadRepository extends JpaRepository<RoommateBoardRead, Long> {

    boolean existsByUserIdAndRoommateBoardId(Long userId, Long roommateBoardId);

    @Query("SELECT r.roommateBoard.id FROM RoommateBoardRead r " +
            "WHERE r.user.id = :userId AND r.roommateBoard.id IN :boardIds")
    List<Long> findReadBoardIds(@Param("userId") Long userId,
                                @Param("boardIds") Collection<Long> boardIds);

    @Modifying
    @Query("DELETE FROM RoommateBoardRead r WHERE r.roommateBoard.id = :boardId")
    void deleteAllByRoommateBoardId(@Param("boardId") Long boardId);
}