package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupOrderCommentRepository extends JpaRepository<GroupOrderComment, Long> {
    List<GroupOrderComment> findByGroupOrder_Id(Long groupOrderId);
}
