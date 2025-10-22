package com.example.appcenter_project.domain.announcement.dto.response;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "공지사항 목록 응답 DTO")
@Builder
@Getter
public class ResponseAnnouncementDto {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "공지사항 제목", example = "기숙사 공지사항")
    private String title;

    @Schema(description = "공지사항 내용 (50자 초과시 50자로 자름)", example = "기숙사 생활 관련 중요한 공지사항입니다. 자세한 내용은...")
    private String content;

    @Schema(description = "생성일", example = "2025-08-05")
    private LocalDateTime createdDate;

    @Schema(description = "수정일", example = "2025-08-05")
    private LocalDateTime updatedDate;

    @Schema(description = "긴급", example = "true")
    private boolean isEmergency;

    private int viewCount = 0;

    private String announcementType;

    public static ResponseAnnouncementDto entityToDto(Announcement announcement) {

        if (announcement instanceof ManualAnnouncement) {
            ManualAnnouncement manualAnnouncement = (ManualAnnouncement) announcement;
            String truncatedContent = manualAnnouncement.getContent();
            if (truncatedContent != null && truncatedContent.length() > 50) {
                truncatedContent = truncatedContent.substring(0, 50);
            }

            return ResponseAnnouncementDto.builder()
                    .id(manualAnnouncement.getId())
                    .title(manualAnnouncement.getTitle())
                    .content(truncatedContent)
                    .createdDate(manualAnnouncement.getCreatedDate())
                    .updatedDate(manualAnnouncement.getModifiedDate())
                    .isEmergency(manualAnnouncement.isEmergency())
                    .viewCount(manualAnnouncement.getViewCount())
                    .announcementType(manualAnnouncement.getAnnouncementType().toValue())
                    .build();
        }


        if (announcement instanceof CrawledAnnouncement) {
            CrawledAnnouncement crawledAnnouncement = (CrawledAnnouncement) announcement;
            String truncatedContent = crawledAnnouncement.getContent();
            if (truncatedContent != null && truncatedContent.length() > 50) {
                truncatedContent = truncatedContent.substring(0, 50);
            }

            return ResponseAnnouncementDto.builder()
                    .id(crawledAnnouncement.getId())
                    .title(crawledAnnouncement.getTitle())
                    .content(truncatedContent)
                    .createdDate(crawledAnnouncement.getCreatedDate())
                    .updatedDate(null)
                    .isEmergency(false)
                    .viewCount(crawledAnnouncement.getViewCount())
                    .announcementType(crawledAnnouncement.getAnnouncementType().toValue())
                    .build();
        }
        return null;
    }
}
