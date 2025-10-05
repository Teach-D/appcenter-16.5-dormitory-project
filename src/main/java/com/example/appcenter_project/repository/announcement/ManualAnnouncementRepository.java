package com.example.appcenter_project.repository.announcement;

import com.example.appcenter_project.entity.announcement.ManualAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManualAnnouncementRepository extends JpaRepository<ManualAnnouncement,Long> {
}
