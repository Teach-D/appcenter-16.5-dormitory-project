package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateRuleDto;
import com.example.appcenter_project.dto.response.roommate.ResponseMyRoommateInfoDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "MyRoommate", description = "내 룸메이트 정보 관련 API")
public interface MyRoommateApiSpecification {

    @Operation(
            summary = "내 룸메이트 정보 조회",
            description = "현재 로그인한 사용자의 룸메이트 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseMyRoommateInfoDto.class))),
                    @ApiResponse(responseCode = "404", description = "룸메이트 정보가 등록되지 않음 (MY_ROOMMATE_NOT_REGISTERED)",
                            content = @Content)
            }
    )
    ResponseEntity<ResponseMyRoommateInfoDto> getMyRoommate(
            @Parameter(hidden = true) CustomUserDetails userDetails
    );

    @Operation(
            summary = "방 규칙 생성",
            description = "방 규칙을 새로 등록하거나 기존 규칙을 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequestRoommateRuleDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성/수정 성공"),
                    @ApiResponse(responseCode = "404", description = "룸메이트 정보 없음 (MY_ROOMMATE_NOT_REGISTERED)")
            }
    )
    ResponseEntity<Void> createRule(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            RequestRoommateRuleDto dto
    );

    @Operation(
            summary = "방 규칙 삭제",
            description = "등록된 방 규칙을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "룸메이트 정보 없음 (MY_ROOMMATE_NOT_REGISTERED)")
            }
    )
    ResponseEntity<Void> deleteRule(@Parameter(hidden = true) CustomUserDetails userDetails);

}
