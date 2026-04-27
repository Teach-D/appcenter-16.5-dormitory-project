package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupOrderCommentRepository extends JpaRepository<GroupOrderComment, Long> {
    List<GroupOrderComment> findByGroupOrder_Id(Long groupOrderId);
    List<GroupOrderComment> findByGroupOrderIdAndParentGroupOrderCommentIsNull(Long groupOrderId);
    Optional<GroupOrderComment> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM GroupOrderComment c WHERE c.groupOrder.id IN :groupOrderIds")
    List<GroupOrderComment> findByGroupOrderIds(@Param("groupOrderIds") List<Long> groupOrderIds);
}