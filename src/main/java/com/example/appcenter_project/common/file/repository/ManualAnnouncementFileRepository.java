package com.example.appcenter_project.common.file.repository;

import com.example.appcenter_project.common.file.entity.AttachedFile;
import com.example.appcenter_project.common.file.entity.ManualAnnouncementFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualAnnouncementFileRepository extends JpaRepository<ManualAnnouncementFile, Long> {
    List<AttachedFile> findByManualAnnouncementId(Long manualAnnouncementId);

    void deleteByAnnouncementId(Long announcementId);
}
