package com.example.appcenter_project.domain.roommate.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseMyRoommateInfoDto {

    private Long matchingId;     // 매칭 ID
    private String name;         // 룸메 이름
    private String dormType;     // 기숙사 타입
    private String college;      // 단과대 이름
    private String imagePath;     // 프로필 이미지 URL

}
