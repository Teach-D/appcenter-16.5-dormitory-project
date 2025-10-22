package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualAnnouncementRepository extends JpaRepository<ManualAnnouncement,Long> {
}
