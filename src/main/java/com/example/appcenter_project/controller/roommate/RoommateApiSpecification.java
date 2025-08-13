package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateCheckListDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Tag(name = "Roommate", description = "룸메이트 게시글 및 체크리스트 관련 API")
public interface RoommateApiSpecification {

    @Operation(
            summary = "룸메이트 체크리스트 및 게시글 작성",
            description = "룸메이트 체크리스트를 작성하고 동시에 게시글을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "룸메이트 게시글 등록 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseRoommatePostDto.class))),
                    @ApiResponse(responseCode = "404", description = "해당 유저가 존재하지 않습니다. (ROOMMATE_USER_NOT_FOUND)")
            }
    )
    ResponseEntity<ResponseRoommatePostDto> createRoommatePost(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody
            @Parameter(description = "룸메이트 체크리스트 요청 DTO", required = true)
            RequestRoommateFormDto requestDto
            // ※ 현재 컨트롤러가 request를 안 받으니 인터페이스도 받지 않음
    );

    @Operation(
            summary = "룸메이트 게시글 최신순 목록 조회",
            description = "작성된 룸메이트 게시글을 최신순으로 조회합니다. (작성자 프로필 이미지 URL 포함)"
    )
    ResponseEntity<List<ResponseRoommatePostDto>> getRoommateBoardList(
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "룸메이트 게시글 단일 조회",
            description = "특정 게시글 ID를 통해 룸메이트 게시글 상세 정보를 조회합니다. (작성자 프로필 이미지 URL 포함)"
    )
    ResponseEntity<ResponseRoommatePostDto> getRoommateBoardDetail(
            @Parameter(description = "조회할 게시글 ID", example = "1") @PathVariable Long boardId,
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "유사한 룸메이트 게시글 추천",
            description = "로그인한 사용자의 체크리스트 기준으로 유사한 게시글을 추천합니다. (작성자 프로필 이미지 URL 포함)"
    )
    ResponseEntity<List<ResponseRoommateSimilarityDto>> getSimilarRoommates(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "룸메이트 체크리스트 및 게시글 수정",
            description = "기존에 작성한 룸메이트 체크리스트 및 게시글을 수정합니다. (작성자 프로필 이미지 URL 포함)"
    )
    ResponseEntity<ResponseRoommatePostDto> updateRoommateCheckListAndBoard(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody
            @Parameter(description = "수정할 룸메이트 체크리스트 요청 DTO", required = true)
            RequestRoommateFormDto requestDto,
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(summary = "룸메이트 게시글 좋아요")
    ResponseEntity<Integer> plusLike(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "좋아요를 누를 게시글 ID", example = "1") @PathVariable Long boardId
    );

    @Operation(summary = "룸메이트 게시글 좋아요 취소")
    ResponseEntity<Integer> minusLike(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "좋아요 취소할 게시글 ID", example = "1") @PathVariable Long boardId
    );

    @Operation(summary = "룸메이트 게시글 주인의 매칭 여부 조회")
    ResponseEntity<Boolean> isBoardOwnerMatched(
            @Parameter(description = "조회할 게시글 ID", example = "1") @PathVariable Long boardId
    );

    @Operation(summary = "룸메이트 게시글 좋아요 여부 조회")
    ResponseEntity<Boolean> isRoommateBoardLiked(
            @Parameter(description = "조회할 게시글 ID", example = "1") @PathVariable Long boardId,
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(summary = "내 룸메이트 체크리스트 단일 조회")
    ResponseEntity<ResponseRoommateCheckListDto> getMyRoommateCheckList(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "최신 10개 중 무작위 1개 조회",
            description = "최신 10개 게시글 중 무작위 1개를 반환합니다. 작성자 프로필 이미지 URL 포함"
    )
    ResponseEntity<ResponseRoommatePostDto> getRandomFromLatest10(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "룸메이트 게시글 최신순 스크롤 조회",
            description = "boardId 내림차순으로 최신순 게시글을 페이지네이션하여 조회합니다. " +
                    "lastId를 기준으로 이전 페이지 데이터를 불러옵니다."
    )
    ResponseEntity<List<ResponseRoommatePostDto>> getRoommateBoardListScroll(
            @Parameter(description = "마지막으로 조회한 게시글 ID (첫 페이지일 경우 비움)", example = "15")
            @RequestParam(required = false) Long lastId,
            @Parameter(description = "한 번에 가져올 데이터 개수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) HttpServletRequest request
    );

    @Operation(
            summary = "유사도 정렬 스크롤 조회",
            description = "로그인한 사용자의 체크리스트를 기준으로 유사도가 높은 게시글부터 내림차순 정렬하여 페이지네이션합니다. " +
                    "유사도가 같을 경우 boardId 내림차순으로 정렬합니다. " +
                    "lastPct와 lastBoardId를 기준으로 커서 페이지네이션을 수행합니다."
    )
    ResponseEntity<List<ResponseRoommateSimilarityDto>> getSimilarRoommateBoardListScrollForMe(
            @Parameter(description = "마지막으로 조회한 게시글의 유사도 퍼센트 (첫 페이지일 경우 비움)", example = "95")
            @RequestParam(required = false) Integer lastPct,
            @Parameter(description = "마지막으로 조회한 게시글 ID (첫 페이지일 경우 비움)", example = "42")
            @RequestParam(required = false) Long lastBoardId,
            @Parameter(description = "한 번에 가져올 데이터 개수", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(hidden = true) HttpServletRequest request
    );
}