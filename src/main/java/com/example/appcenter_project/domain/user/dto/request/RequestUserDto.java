package com.example.appcenter_project.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "유저 정보 입력")
@Getter
public class RequestUserDto {

    private String name;

    // 필드명과 설명을 올바르게 매치
    @Schema(description = "대학",
            allowableValues = {"인문대학", "자연과학대학", "사회과학대학", "글로벌정경대학",
                    "공과대학", "정보기술대학", "경영대학", "예술체육대학",
                    "사범대학", "도시과학대학", "생명과학기술대학", "융합자유전공대학",
                    "동북아국제통상물류학부", "법학부", "계약학과"})
    private String college;  // 대학 정보

    @Schema(description = "기숙사 타입",
            allowableValues = {"2기숙사", "3기숙사"})
    private String dormType;  // 기숙사 타입

    private int penalty;

}