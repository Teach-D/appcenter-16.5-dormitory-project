package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long>, JpaSpecificationExecutor<GroupOrder> {
}
