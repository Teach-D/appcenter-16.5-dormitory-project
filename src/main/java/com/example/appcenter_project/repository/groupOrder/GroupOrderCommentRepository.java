package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupOrderCommentRepository extends JpaRepository<GroupOrderComment, Long> {
    List<GroupOrderComment> findByGroupOrder_Id(Long groupOrderId);
    List<GroupOrderComment> findByGroupOrder_IdAndParentGroupOrderCommentIsNull(Long groupOrderId);
    Optional<GroupOrderComment> findByIdAndUserId(Long id, Long userId);
}
