package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrderChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupOrderChatRepository extends JpaRepository<GroupOrderChat, Long> {
    List<GroupOrderChat> findByGroupOrderChatRoom_id(Long id);
}
