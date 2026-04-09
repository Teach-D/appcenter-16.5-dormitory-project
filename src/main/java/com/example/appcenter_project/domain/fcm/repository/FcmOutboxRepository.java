package com.example.appcenter_project.domain.fcm.repository;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.OutboxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FcmOutboxRepository extends JpaRepository<FcmOutbox, Long> {

    List<FcmOutbox> findByStatusAndNextRetryAtBefore(OutboxStatus status, LocalDateTime now);

    List<FcmOutbox> findByStatusInAndNextRetryAtBefore(List<OutboxStatus> statuses, LocalDateTime now);

    Page<FcmOutbox> findByStatusIn(List<OutboxStatus> statuses, Pageable pageable);
}
