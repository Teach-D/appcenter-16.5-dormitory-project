package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
