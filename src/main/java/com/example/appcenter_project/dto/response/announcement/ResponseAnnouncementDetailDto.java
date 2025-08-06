package com.example.appcenter_project.dto.response.announcement;

import com.example.appcenter_project.entity.announcement.Announcement;
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

    public static ResponseAnnouncementDetailDto entityToDto(Announcement announcement) {
        return ResponseAnnouncementDetailDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .writer(announcement.getWriter())
                .content(announcement.getContent())
                .viewCount(announcement.getViewCount())
                .createdDate(announcement.getCreatedDate().toLocalDate())
                .updatedDate(announcement.getModifiedDate().toLocalDate())
                .isEmergency(announcement.getIsEmergency())
                .build();
    }
}
