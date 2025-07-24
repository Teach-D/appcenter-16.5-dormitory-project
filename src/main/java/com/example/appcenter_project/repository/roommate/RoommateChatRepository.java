package com.example.appcenter_project.repository.roommate;

import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommateChatRepository extends JpaRepository<RoommateChattingChat, Long> {
    List<RoommateChattingChat> findAllByRoommateChattingRoom_Id(Long roomId);
}
