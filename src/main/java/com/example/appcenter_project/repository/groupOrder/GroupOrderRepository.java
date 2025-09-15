package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import jakarta.persistence.LockModeType;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long>, JpaSpecificationExecutor<GroupOrder>, GroupOrderQuerydslRepository {
    Optional<GroupOrder> findByGroupOrderChatRoom_id(Long id);
    boolean existsByTitle(String title);
    Optional<GroupOrder> findByIdAndUserId(Long id, Long userId);
    List<GroupOrder> findByUserId(Long  userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GroupOrder g WHERE g.id = :id")
    Optional<GroupOrder> findByIdWithLock(@Param("id") Long id);
}
