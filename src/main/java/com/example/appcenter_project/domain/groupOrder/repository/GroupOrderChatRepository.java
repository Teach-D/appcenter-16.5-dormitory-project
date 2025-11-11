package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupOrderChatRepository extends JpaRepository<GroupOrderChat, Long> {
    List<GroupOrderChat> findByGroupOrderChatRoom_id(Long id);
}
