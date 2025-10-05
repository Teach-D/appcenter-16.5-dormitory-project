package com.example.appcenter_project.dto.response.announcement;

import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.announcement.CrawledAnnouncement;
import com.example.appcenter_project.entity.announcement.ManualAnnouncement;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "공지사항 상세 응답 DTO")
@Builder
@Getter
public class ResponseAnnouncementDetailDto {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    private String category;

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

    private String announcementType;

    public static ResponseAnnouncementDetailDto entityToDto(Announcement announcement) {

        if (announcement instanceof ManualAnnouncement) {
            ManualAnnouncement manualAnnouncement = (ManualAnnouncement) announcement;

            return ResponseAnnouncementDetailDto.builder()
                    .id(manualAnnouncement.getId())
                    .title(manualAnnouncement.getTitle())
                    .content(manualAnnouncement.getContent())
                    .createdDate(LocalDate.from(manualAnnouncement.getCreatedDate()))
                    .updatedDate(manualAnnouncement.getModifiedDate().toLocalDate())
                    .isEmergency(manualAnnouncement.isEmergency())
                    .viewCount(manualAnnouncement.getViewCount())
                    .announcementType(manualAnnouncement.getAnnouncementType().toValue())
                    .build();
        }


        if (announcement instanceof CrawledAnnouncement) {
            CrawledAnnouncement crawledAnnouncement = (CrawledAnnouncement) announcement;

            return ResponseAnnouncementDetailDto.builder()
                    .id(crawledAnnouncement.getId())
                    .category(crawledAnnouncement.getCategory())
                    .title(crawledAnnouncement.getTitle())
                    .content(crawledAnnouncement.getContent())
                    .createdDate(LocalDate.from(crawledAnnouncement.getCreatedDate()))
                    .updatedDate(null)
                    .isEmergency(false)
                    .viewCount(crawledAnnouncement.getViewCount())
                    .announcementType(AnnouncementType.DORMITORY.toValue())
                    .build();
        }
        return null;
    }
}
