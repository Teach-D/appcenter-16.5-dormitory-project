package com.example.appcenter_project.domain.announcement.dto.request;

import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class RequestAnnouncementDto {

    private AnnouncementCategory category;
    private String title;
    private String writer;
    private String content;
    private Boolean isEmergency;

}
