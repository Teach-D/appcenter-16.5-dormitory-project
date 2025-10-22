package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawledAnnouncementRepository extends JpaRepository <CrawledAnnouncement, Long> {
    boolean existsByNumber(String number);
}
