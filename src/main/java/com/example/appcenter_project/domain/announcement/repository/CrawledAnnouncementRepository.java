package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.ScheduleExtractStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CrawledAnnouncementRepository extends JpaRepository<CrawledAnnouncement, Long> {
    boolean existsByNumber(String number);
    Optional<CrawledAnnouncement> findByNumber(String number);

    @Query("SELECT c FROM CrawledAnnouncement c WHERE c.scheduleExtractStatus IN :statuses " +
            "AND c.scheduleExtractRetryCount < 3 ORDER BY c.id ASC")
    List<CrawledAnnouncement> findScheduleExtractTargets(
            @Param("statuses") List<ScheduleExtractStatus> statuses,
            Pageable pageable);
}