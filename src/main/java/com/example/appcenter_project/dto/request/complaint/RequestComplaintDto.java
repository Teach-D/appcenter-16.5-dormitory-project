package com.example.appcenter_project.dto.request.complaint;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "민원 등록/수정 요청 DTO")
public class RequestComplaintDto {

    @Schema(description = "민원 유형", 
            example = "기물", 
            allowableValues = {"기물", "시설", "청소/위생", "소음", "기타"})
    private String type;        // 유형 (기물, 시설, 청소/위생, 소음, 기타)
    
    @Schema(description = "기숙사 구분", 
            example = "1기숙사", 
            allowableValues = {"1기숙사", "2기숙사", "3기숙사"})
    private String dormType;    // 기숙사 (1기숙사, 2기숙사, 3기숙사)
    
    @Schema(description = "사생번호", example = "101-201")
    private String caseNumber;  // 사생번호
    
    @Schema(description = "연락처", example = "010-1234-5678")
    private String contact;     // 연락처
    
    @Schema(description = "민원 제목", example = "화장실 변기가 막혔습니다")
    private String title;       // 민원 제목
    
    @Schema(description = "민원 내용", 
            example = "3층 301호 화장실 변기가 막혀서 사용할 수 없습니다. 빠른 수리 부탁드립니다.")
    private String content;     // 민원 내용
}
