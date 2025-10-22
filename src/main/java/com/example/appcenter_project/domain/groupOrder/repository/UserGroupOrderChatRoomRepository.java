package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.entity.UserGroupOrderChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserGroupOrderChatRoomRepository extends JpaRepository<UserGroupOrderChatRoom, Long> {
    Optional<UserGroupOrderChatRoom> findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(Long user_id, Long group_id);
    List<UserGroupOrderChatRoom> findUserGroupOrderChatRoomByUser_Id(Long user_id);
}
