package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderSort;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnnouncementQuerydslRepository {

    List<Announcement> findAnnouncementComplex(AnnouncementType type, AnnouncementCategory category, String search);

}
