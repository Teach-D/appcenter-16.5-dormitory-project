package com.example.appcenter_project.domain.announcement.dto.request;

import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import lombok.Getter;

@Getter
public class RequestAnnouncementDto {

    private String title;
    private String writer;
    private String content;
    private Boolean isEmergency;

    public static ManualAnnouncement of(RequestAnnouncementDto requestAnnouncementDto, AnnouncementType announcementType) {
        return ManualAnnouncement.builder()
                .title(requestAnnouncementDto.getTitle())
                .writer(requestAnnouncementDto.getWriter())
                .content(requestAnnouncementDto.getContent())
                .isEmergency(requestAnnouncementDto.getIsEmergency())
                .announcementType(announcementType)
                .build();
    }

}
