package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateFormDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommatePostDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam
            @Parameter(description = "유저 ID", required = true, example = "1") Long userId,

            @RequestBody
            @Parameter(description = "룸메이트 체크리스트 요청 DTO", required = true)
            RequestRoommateFormDto requestDto);
}
