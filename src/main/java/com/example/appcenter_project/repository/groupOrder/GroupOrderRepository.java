package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long>, JpaSpecificationExecutor<GroupOrder> {
    Optional<GroupOrder> findByGroupOrderChatRoom_id(Long id);
}
