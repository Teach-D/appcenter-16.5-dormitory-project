package com.example.appcenter_project.repository.file;

import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.entity.file.ManualAnnouncementFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualAnnouncementFileRepository extends JpaRepository<ManualAnnouncementFile, Long> {
    List<AttachedFile> findByManualAnnouncementId(Long manualAnnouncementId);

    void deleteByAnnouncementId(Long announcementId);
}
