package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import jakarta.persistence.LockModeType;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long>, JpaSpecificationExecutor<GroupOrder>, GroupOrderQuerydslRepository {
    Optional<GroupOrder> findByGroupOrderChatRoom_id(Long id);
    boolean existsByTitle(String title);
    Optional<GroupOrder> findByIdAndUserId(Long id, Long userId);
    List<GroupOrder> findByUserId(Long  userId);
    List<GroupOrder> findByDeadlineBetween(LocalDateTime now, LocalDateTime oneHourLater);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GroupOrder g WHERE g.id = :id")
    Optional<GroupOrder> findByIdWithLock(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE GroupOrder g SET g.groupOrderViewCount = g.groupOrderViewCount + :count WHERE g.id = :id")
    int incrementViewCountBy(@Param("id") Long id, @Param("count") Long count);

    @Query("SELECT g FROM GroupOrder g " +
            "WHERE g.deadline > :now " +
            "AND g.deadline <= :oneHourLater")
    List<GroupOrder> findGroupOrdersEndingSoon(
            @Param("now") LocalDateTime now,
            @Param("oneHourLater") LocalDateTime oneHourLater
    );
}
