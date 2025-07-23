package com.example.appcenter_project.repository.roommate;

import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommateChattingChatRepository extends JpaRepository<RoommateChattingChat, Long> {
    List<RoommateChattingChat> findAllByRoommateChattingRoom_Id(Long roomId);
    List<RoommateChattingChat> findByRoommateChattingRoom(RoommateChattingRoom chatRoom);

    List<RoommateChattingChat> findByRoommateChattingRoomAndReadByReceiverFalse(RoommateChattingRoom chatRoom);

    List<RoommateChattingChat> findByRoommateChattingRoomAndMemberNotAndReadByReceiverFalse(RoommateChattingRoom chatRoom, com.example.appcenter_project.entity.user.User member);

}
