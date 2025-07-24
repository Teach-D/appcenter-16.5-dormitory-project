package com.example.appcenter_project.repository.roommate;

import com.example.appcenter_project.entity.roommate.RoommateBoard;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommateChattingRoomRepository extends JpaRepository<RoommateChattingRoom, Long> {
    boolean existsByRoommateBoardAndGuest(RoommateBoard board, User guest);
    List<RoommateChattingRoom> findAllByHostOrGuest(User host, User guest);
}

