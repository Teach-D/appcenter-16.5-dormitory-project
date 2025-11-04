package com.example.appcenter_project.domain.calender.controller;

import com.example.appcenter_project.domain.calender.dto.request.RequestCalenderDto;
import com.example.appcenter_project.domain.calender.dto.response.ResponseCalenderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "캘린더 API", description = "캘린더 관리 관련 API")
public interface CalenderApiSpecification {

    @Operation(
            summary = "캘린더 생성",
            description = "새로운 캘린더 일정을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "캘린더 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
            }
    )
    ResponseEntity<Void> saveCalender(
            @Valid @RequestBody
            @Parameter(description = "캘린더 생성 정보", required = true) RequestCalenderDto requestCalenderDto);

    @Operation(
            summary = "모든 캘린더 조회",
            description = "모든 캘린더 일정을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseCalenderDto.class))
                            )
                    )
            }
    )
    ResponseEntity<List<ResponseCalenderDto>> getAllCalenders();

    @Operation(
            summary = "특정 년월 캘린더 조회",
            description = "지정된 년월의 캘린더 일정만 조회합니다. 해당 월에 시작/종료/걸쳐있는 모든 일정을 포함합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseCalenderDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 년월 값")
            }
    )
    ResponseEntity<List<ResponseCalenderDto>> getCalendersByYearAndMonth(
            @RequestParam
            @Parameter(description = "조회할 년도", required = true, example = "2025") int year,
            @RequestParam
            @Parameter(description = "조회할 월 (1-12)", required = true, example = "8") int month);

    @Operation(
            summary = "특정 캘린더 조회",
            description = "ID로 특정 캘린더 일정을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseCalenderDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "캘린더를 찾을 수 없음")
            }
    )
    ResponseEntity<ResponseCalenderDto> getCalender(
            @PathVariable
            @Parameter(description = "캘린더 ID", required = true, example = "1") Long calenderId);

    @Operation(
            summary = "캘린더 수정",
            description = "기존 캘린더 일정을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "404", description = "캘린더를 찾을 수 없음"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
            }
    )
    ResponseEntity<Void> updateCalender(
            @PathVariable
            @Parameter(description = "캘린더 ID", required = true, example = "1") Long calenderId,
            @Valid @RequestBody
            @Parameter(description = "수정할 캘린더 정보", required = true) RequestCalenderDto requestCalenderDto);

    @Operation(
            summary = "캘린더 삭제",
            description = "캘린더 일정을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "캘린더를 찾을 수 없음")
            }
    )
    ResponseEntity<Void> deleteCalender(
            @PathVariable
            @Parameter(description = "캘린더 ID", required = true, example = "1") Long calenderId);
}
