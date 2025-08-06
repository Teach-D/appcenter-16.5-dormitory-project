package com.example.appcenter_project.repository.announcement;

import com.example.appcenter_project.entity.announcement.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
