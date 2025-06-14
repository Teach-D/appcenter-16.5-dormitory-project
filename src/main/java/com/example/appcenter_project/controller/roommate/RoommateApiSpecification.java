package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateSimilarityDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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
                    @ApiResponse(responseCode = "404", description = "해당 유저가 존재하지 않습니다. (ROOMMATE_USER_NOT_FOUND)",
                            content = @Content(examples = {}))
            }
    )
    ResponseEntity<ResponseRoommatePostDto> createRoommatePost(
            @Parameter(hidden = true) CustomUserDetails userDetails,

            @RequestBody
            @Parameter(description = "룸메이트 체크리스트 요청 DTO", required = true)
            RequestRoommateFormDto requestDto
    );

    @Operation(
            summary = "룸메이트 게시글 최신순 목록 조회",
            description = "작성된 룸메이트 게시글을 최신순으로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseRoommatePostDto.class))),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않습니다. (ROOMMATE_BOARD_NOT_FOUND)",
                            content = @Content(examples = {}))
            }
    )
    ResponseEntity<List<ResponseRoommatePostDto>> getRoommateBoardList();

    @Operation(
            summary = "룸메이트 게시글 단일 조회",
            description = "특정 게시글 ID를 통해 룸메이트 게시글 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseRoommatePostDto.class))),
                    @ApiResponse(responseCode = "404", description = "해당 게시글이 존재하지 않습니다. (ROOMMATE_BOARD_NOT_FOUND)",
                            content = @Content(examples = {}))
            }
    )
    ResponseEntity<ResponseRoommatePostDto> getRoommateBoardDetail(
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable Long boardId
    );

    @Operation(
            summary = "유사한 룸메이트 게시글 추천",
            description = "로그인한 사용자의 체크리스트 기준으로 유사한 게시글을 추천합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 게시글 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseRoommateSimilarityDto.class))),
                    @ApiResponse(responseCode = "404", description = "유사도 비교할 게시글이 없습니다 (ROOMMATE_NO_SIMILAR_BOARD)",
                            content = @Content(examples = {}))
            }
    )
    ResponseEntity<List<ResponseRoommateSimilarityDto>> getSimilarRoommates(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

}
