package com.example.appcenter_project.dto.response.roommate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseRoommateBoardDto {
    private Long roommateBoardId;  // 생성된 게시글 ID
    private String message;        // 응답 메시지

    //게시글 ID를 기반으로 응답 객체 생성
    public static ResponseRoommateBoardDto of(Long id) {
        return new ResponseRoommateBoardDto(id, "룸메이트 게시글이 성공적으로 생성되었습니다.");
    }
}
