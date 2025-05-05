package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateBoardDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateBoardDto;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.roommate.RoommateBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roommate/board")
@Tag(name = "RoommateBoard", description = "룸메이트 게시판 API")
public class RoommateBoardController {

    private final RoommateBoardService roommateBoardService;

    /**
     * 룸메이트 게시글 생성 API
     * @param user 로그인한 사용자 정보 (JWT 인증을 통해 주입됨)
     * @param requestDto 게시글 + 체크리스트 정보
     * @return 생성된 게시글 ID와 메시지를 담은 응답
     */
    @PostMapping
    @Operation(summary = "룸메이트 게시판 작성", description = "체크리스트를 기반으로 게시글을 작성합니다.")
    public ResponseEntity<ResponseRoommateBoardDto> createBoard(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody RequestRoommateBoardDto requestDto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roommateBoardService.createBoard(user.getId(), requestDto));
    }
}
