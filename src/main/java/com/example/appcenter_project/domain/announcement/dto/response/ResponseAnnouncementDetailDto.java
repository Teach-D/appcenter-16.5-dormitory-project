package com.example.appcenter_project.domain.announcement.dto.response;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Schema(description = "공지사항 상세 응답 DTO")
@Builder
@Getter
public class ResponseAnnouncementDetailDto {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리")
    private AnnouncementCategory category;
    
    @Schema(description = "작성 주체")
    private AnnouncementType announcementType;
    
    @Schema(description = "공지사항 제목", example = "기숙사 공지사항")
    private String title;

    @Schema(description = "작성자", example = "관리자")
    private String writer;

    @Schema(description = "공지사항 내용", example = "공지사항 내용입니다.")
    private String content;

    @Schema(description = "조회수", example = "100")
    private int viewCount;

    @Schema(description = "생성일", example = "2025-08-05")
    private LocalDate createdDate;

    @Schema(description = "수정일", example = "2025-08-05")
    private LocalDate updatedDate;

    @Schema(description = "긴급", example = "true")
    private boolean isEmergency;

    private String number;
    
    private String link;

    public static ResponseAnnouncementDetailDto from(Announcement announcement) {

        if (announcement instanceof ManualAnnouncement) {
            ManualAnnouncement manualAnnouncement = (ManualAnnouncement) announcement;

            return ResponseAnnouncementDetailDto.builder()
                    .id(manualAnnouncement.getId())
                    .category(manualAnnouncement.getAnnouncementCategory())
                    .title(manualAnnouncement.getTitle())
                    .writer(announcement.getWriter())
                    .content(manualAnnouncement.getContent())
                    .createdDate(LocalDate.from(manualAnnouncement.getCreatedDate()))
                    .updatedDate(manualAnnouncement.getModifiedDate().toLocalDate())
                    .isEmergency(manualAnnouncement.isEmergency())
                    .viewCount(manualAnnouncement.getViewCount())
                    .announcementType(manualAnnouncement.getAnnouncementType())
                    .build();
        }


        if (announcement instanceof CrawledAnnouncement) {
            CrawledAnnouncement crawledAnnouncement = (CrawledAnnouncement) announcement;

            return ResponseAnnouncementDetailDto.builder()
                    .id(crawledAnnouncement.getId())
                    .category(crawledAnnouncement.getAnnouncementCategory())
                    .writer(announcement.getWriter())
                    .title(crawledAnnouncement.getTitle())
                    .content(crawledAnnouncement.getContent())
                    .createdDate(crawledAnnouncement.getCrawledDate())
                    .updatedDate(null)
                    .isEmergency(false)
                    .viewCount(crawledAnnouncement.getViewCount())
                    .number(crawledAnnouncement.getNumber())
                    .announcementType(AnnouncementType.DORMITORY)
                    .link(crawledAnnouncement.getLink())
                    .build();
        }
        return null;
    }
}
