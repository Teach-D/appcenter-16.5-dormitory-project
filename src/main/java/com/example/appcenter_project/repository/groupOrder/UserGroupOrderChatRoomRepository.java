package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGroupOrderChatRoomRepository extends JpaRepository<UserGroupOrderChatRoom, Long> {
    Optional<UserGroupOrderChatRoom> findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(Long user_id, Long group_id);
}
