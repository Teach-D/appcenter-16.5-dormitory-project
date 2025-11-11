package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoommateChattingRoomRepository extends JpaRepository<RoommateChattingRoom, Long> {
    boolean existsByRoommateBoardAndGuest(RoommateBoard board, User guest);
    List<RoommateChattingRoom> findAllByHostOrGuest(User host, User guest);
    boolean existsRoommateChattingRoomByGuestAndHost(User guest, User host);
    boolean existsRoommateChattingRoomByHostAndGuest(User host, User guest);

    Optional<RoommateChattingRoom> findByHostAndGuest(User guest, User host);

    Optional<RoommateChattingRoom> findByGuestAndHost(User guest, User host);
}

