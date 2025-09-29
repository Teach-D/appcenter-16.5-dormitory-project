package com.example.appcenter_project.dto.response.announcement;

import com.example.appcenter_project.entity.announcement.Announcement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

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
    private LocalDate createdDate;

    @Schema(description = "수정일", example = "2025-08-05")
    private LocalDate updatedDate;

    @Schema(description = "긴급", example = "true")
    private boolean isEmergency;

    private int viewCount = 0;

    private String announcementType;

    public static ResponseAnnouncementDto entityToDto(Announcement announcement) {
        String truncatedContent = announcement.getContent();
        if (truncatedContent != null && truncatedContent.length() > 50) {
            truncatedContent = truncatedContent.substring(0, 50);
        }
        
        return ResponseAnnouncementDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(truncatedContent)
                .createdDate(announcement.getCreatedDate().toLocalDate())
                .updatedDate(announcement.getModifiedDate().toLocalDate())
                .isEmergency(announcement.getIsEmergency())
                .viewCount(announcement.getViewCount())
                .announcementType(announcement.getAnnouncementType().toValue())
                .build();
    }
}
