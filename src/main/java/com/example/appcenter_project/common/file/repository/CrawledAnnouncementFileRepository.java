package com.example.appcenter_project.common.file.repository;

import com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrawledAnnouncementFileRepository extends JpaRepository<CrawledAnnouncementFile, Long> {
    List<CrawledAnnouncementFile> findByCrawledAnnouncementId(Long announcementId);
}
