package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long>, AnnouncementQuerydslRepository {
}
