package com.example.appcenter_project.repository.file;

import com.example.appcenter_project.entity.file.CrawledAnnouncementFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrawledAnnouncementFileRepository extends JpaRepository<CrawledAnnouncementFile, Long> {
    List<CrawledAnnouncementFile> findByCrawledAnnouncementId(Long announcementId);
}
