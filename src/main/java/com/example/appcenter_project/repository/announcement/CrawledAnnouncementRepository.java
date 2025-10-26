package com.example.appcenter_project.repository.announcement;

import com.example.appcenter_project.entity.announcement.CrawledAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrawledAnnouncementRepository extends JpaRepository <CrawledAnnouncement, Long> {
    boolean existsByNumber(String number);
    Optional<CrawledAnnouncement> findByNumber(String number);
}
