package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoommateChattingRoomRepository extends JpaRepository<RoommateChattingRoom, Long> {
    boolean existsByRoommateBoardAndGuest(RoommateBoard board, User guest);
    List<RoommateChattingRoom> findAllByHostOrGuest(User host, User guest);
    boolean existsRoommateChattingRoomByGuestAndHost(User guest, User host);
    boolean existsRoommateChattingRoomByHostAndGuest(User host, User guest);

    Optional<RoommateChattingRoom> findByHostAndGuest(User guest, User host);

    Optional<RoommateChattingRoom> findByGuestAndHost(User guest, User host);

    @Query("""
            SELECT r FROM RoommateChattingRoom r
            JOIN FETCH r.host JOIN FETCH r.guest
            WHERE (r.host.id = :userId AND r.hostLeft = false)
               OR (r.guest.id = :userId AND r.guestLeft = false)
            """)
    List<RoommateChattingRoom> findActiveRoomsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RoommateChattingRoom r SET r.roommateBoard = null WHERE r.roommateBoard.id = :boardId")
    void detachBoard(@Param("boardId") Long boardId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RoommateChattingRoom r SET r.guestChecklist = null WHERE r.guestChecklist.id = :checkListId")
    void detachGuestChecklist(@Param("checkListId") Long checkListId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RoommateChattingRoom r SET r.hostChecklist = null WHERE r.hostChecklist.id = :checkListId")
    void detachHostChecklist(@Param("checkListId") Long checkListId);
}

