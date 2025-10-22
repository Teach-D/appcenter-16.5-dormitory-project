package com.example.appcenter_project.domain.complaint.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "민원 등록/수정 요청 DTO")
public class RequestComplaintDto {

    @Schema(description = "민원 유형", 
            example = "소음",
            allowableValues = {"소음", "흡연", "음주", "호실 변경", "공용공간"})
    private String type;
    
    @Schema(description = "기숙사 구분", 
            example = "1기숙사", 
            allowableValues = {"1기숙사", "2기숙사", "3기숙사"})
    private String dormType;    // 기숙사 (1기숙사, 2기숙사, 3기숙사)

    @Schema(description = "동", example = "A동")
    private String building;

    @Schema(description = "층", example = "3층")
    private String floor;

    @Schema(description = "호수", example = "301호")
    private String roomNumber;

    @Schema(description = "침대번호", example = "1번")
    private String bedNumber;
    
    @Schema(description = "민원 제목", example = "화장실 변기가 막혔습니다")
    private String title;       // 민원 제목
    
    @Schema(description = "민원 내용", 
            example = "3층 301호 화장실 변기가 막혀서 사용할 수 없습니다. 빠른 수리 부탁드립니다.")
    private String content;     // 민원 내용

    @Schema(description = "구체적 장소",
            example = "3층 301호",
            required = true)
    private String specificLocation; // 구체적 장소

    @Schema(description = "사건 발생 날짜",
            example = "2024-01-15", 
            required = true)
    private String incidentDate; // 사건 발생 날짜

    @Schema(description = "사건 발생 시간 (필수)", 
            example = "14:30", 
            required = true)
    private String incidentTime; // 사건 발생 시간

    @Schema(description = "개인정보 동의 여부", example = "true")
    private boolean isPrivacyAgreed;

}
