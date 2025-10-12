package com.example.appcenter_project.dto.request.announement;

import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.announcement.ManualAnnouncement;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import lombok.Getter;

@Getter
public class RequestAnnouncementDto {

    private String title;
    private String writer;
    private String content;
    private Boolean isEmergency;

    public static ManualAnnouncement dtoToEntity(RequestAnnouncementDto requestAnnouncementDto, AnnouncementType announcementType) {
        return ManualAnnouncement.builder()
                .title(requestAnnouncementDto.getTitle())
                .writer(requestAnnouncementDto.getWriter())
                .content(requestAnnouncementDto.getContent())
                .isEmergency(requestAnnouncementDto.getIsEmergency())
                .announcementType(announcementType)
                .build();
    }

}
