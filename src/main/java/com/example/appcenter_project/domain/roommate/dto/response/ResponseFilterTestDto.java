package com.example.appcenter_project.domain.roommate.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResponseFilterTestDto {
    private Long boardId;
    private String boardTitle;
    private Long boardAuthorId;
    private String boardAuthorName;
    private int totalFilters;
    private int matchedUsers;
    private List<MatchedUserInfo> matchedUserList;
    private List<FilteredOutUserInfo> filteredOutUserList;

    @Getter
    @Builder
    public static class MatchedUserInfo {
        private Long userId;
        private String userName;
        private String reason; // "모든 필터 조건 일치"
    }

    @Getter
    @Builder
    public static class FilteredOutUserInfo {
        private Long userId;
        private String userName;
        private String reason; // "게시글 작성자 본인", "ROOMMATE 알림 비활성화", "dormType 불일치" 등
    }
}

