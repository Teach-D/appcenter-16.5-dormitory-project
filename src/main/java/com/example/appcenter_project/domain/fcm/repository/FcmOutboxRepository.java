package com.example.appcenter_project.domain.fcm.repository;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.OutboxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FcmOutboxRepository extends JpaRepository<FcmOutbox, Long> {

    List<FcmOutbox> findByStatusAndNextRetryAtBefore(OutboxStatus status, LocalDateTime now);

    List<FcmOutbox> findByStatusInAndNextRetryAtBefore(List<OutboxStatus> statuses, LocalDateTime now);

    @Query("SELECT o FROM FcmOutbox o WHERE o.status IN :statuses AND o.nextRetryAt < :now AND (o.expiredAt IS NULL OR o.expiredAt > :now) ORDER BY o.id ASC")
    List<FcmOutbox> findChunk(@Param("statuses") List<OutboxStatus> statuses, @Param("now") LocalDateTime now, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE FcmOutbox o SET o.status = com.example.appcenter_project.domain.fcm.enums.OutboxStatus.EXPIRED WHERE o.status IN :statuses AND o.expiredAt IS NOT NULL AND o.expiredAt <= :now")
    int bulkMarkExpired(@Param("statuses") List<OutboxStatus> statuses, @Param("now") LocalDateTime now);

    Page<FcmOutbox> findByStatusIn(List<OutboxStatus> statuses, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE FcmOutbox o SET o.status = :status WHERE o.id IN :ids")
    void bulkUpdateStatus(@Param("ids") List<Long> ids, @Param("status") OutboxStatus status);

    default void bulkMarkProcessing(List<Long> ids) {
        bulkUpdateStatus(ids, OutboxStatus.PROCESSING);
    }

    @Modifying(clearAutomatically = true)
    @Query("UPDATE FcmOutbox o SET o.status = :status, o.lastErrorCode = :errorCode WHERE o.id IN :ids")
    void bulkUpdateStatusWithError(@Param("ids") List<Long> ids, @Param("status") OutboxStatus status, @Param("errorCode") String errorCode);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE FcmOutbox o SET o.status = com.example.appcenter_project.domain.fcm.enums.OutboxStatus.FAILED, o.retryCount = o.retryCount + 1, o.lastErrorCode = :errorCode, o.nextRetryAt = :nextRetryAt WHERE o.id IN :ids")
    void bulkMarkFailed(@Param("ids") List<Long> ids, @Param("errorCode") String errorCode, @Param("nextRetryAt") LocalDateTime nextRetryAt);
}
