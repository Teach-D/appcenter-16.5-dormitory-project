package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long>, JpaSpecificationExecutor<GroupOrder> {
    Optional<GroupOrder> findByGroupOrderChatRoom_id(Long id);
    boolean existsByTitle(String title);
    Optional<GroupOrder> findByIdAndUserId(Long id, Long userId);
    List<GroupOrder> findByUserId(Long  userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT go FROM GroupOrder go where go.id = :id")
    Optional<GroupOrder> findByIdWithPessimisticLock(Long id);
    
}
