package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "RoommateNotificationFilter", description = "룸메이트 알림 필터 설정 관련 API")
public interface RoommateNotificationFilterApiSpecification {

    @Operation(
            summary = "룸메이트 알림 필터 설정/수정",
            description = """
                    룸메이트 게시글 알림을 받을 조건을 설정합니다. 필터가 이미 존재하면 업데이트됩니다.
                    
                    ### 필터링 로직
                    - 모든 필터 조건은 **AND**로 연결됩니다 (모든 조건을 만족해야 알림을 받습니다)
                    - 필터가 `null`이면 해당 조건은 체크하지 않습니다
                    
                    ### 필터 조건 설명
                    
                    **기본 정보**
                    - `dormType`: 기숙사 종류 (단일 선택)
                    - `dormPeriodDays`: 기숙사 비상주기간 (다중 선택 가능)
                      - 선택한 날짜는 비상주 기간입니다
                      - 예: ["월", "화"] 선택 = 월, 화에 비상주 = 수, 목, 금, 토, 일에 상주하는 사람의 글만 알림
                      - 게시글 작성자의 상주기간과 필터의 비상주기간이 겹치면 안 됨 (교집합 없어야 함)
                    - `colleges`: 단과대 (다중 선택 가능, 하나라도 일치하면 OK)
                    
                    **생활 습관**
                    - `smoking`: 흡연여부
                    - `snoring`: 코골이유무
                    - `toothGrind`: 이갈이유무
                    - `sleeper`: 잠귀
                    
                    **생활 리듬**
                    - `showerHour`: 샤워 시기
                    - `showerTime`: 샤워 시간
                    - `bedTime`: 취침시기
                    
                    **성향**
                    - `arrangement`: 정리정돈
                    - `religions`: 종교 (다중 선택 가능, 하나라도 일치하면 OK)
                    
                    ### 예시
                    ```json
                    {
                      "dormType": "2기숙사",
                      "dormPeriodDays": ["월", "화"],
                      "colleges": ["공과대", "사범대"],
                      "smoking": "안피워요",
                      "religions": ["기독교", "불교"]
                    }
                    ```
                    → 2기숙사에 거주하고, 월/화에 비상주하며, 공과대 또는 사범대 소속이며, 
                       흡연하지 않고, 기독교 또는 불교인 사람의 게시글만 알림을 받습니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "필터 설정/수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)")
            }
    )
    ResponseEntity<Void> saveOrUpdateFilter(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Parameter(description = "룸메이트 알림 필터 설정 DTO", required = true)
            RequestRoommateNotificationFilterDto requestDto
    );

    @Operation(
            summary = "룸메이트 알림 필터 조회",
            description = """
                    현재 사용자의 룸메이트 알림 필터 설정을 조회합니다.
                    
                    필터가 설정되지 않은 경우 모든 필드가 `null`로 반환됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "필터 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseRoommateNotificationFilterDto.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)")
            }
    )
    ResponseEntity<ResponseRoommateNotificationFilterDto> getFilter(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "룸메이트 알림 필터 삭제",
            description = """
                    현재 사용자의 룸메이트 알림 필터 설정을 삭제합니다.
                    
                    필터 삭제 후에는 룸메이트 게시글 알림을 받지 않습니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "필터 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)")
            }
    )
    ResponseEntity<Void> deleteFilter(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "필터 조건에 맞는 게시글 목록 조회",
            description = """
                    현재 사용자가 설정한 필터 조건에 맞는 룸메이트 게시글 목록을 조회합니다.
                    
                    ### 필터링 조건
                    - 본인이 작성한 게시글은 제외됩니다
                    - ROOMMATE 알림을 받지 않도록 설정한 경우 빈 리스트가 반환됩니다
                    - 필터가 설정되지 않은 경우 빈 리스트가 반환됩니다
                    
                    ### 필터링 로직
                    - 모든 필터 조건은 **AND**로 연결됩니다
                    - 필터가 `null`이면 해당 조건은 체크하지 않습니다
                    - 비상주기간 필터: 게시글 작성자의 상주기간과 필터의 비상주기간이 겹치면 안 됨
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "필터링된 게시글 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseRoommatePostDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (USER_NOT_FOUND)")
            }
    )
    ResponseEntity<List<ResponseRoommatePostDto>> getFilteredBoards(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(hidden = true) HttpServletRequest request
    );
}

