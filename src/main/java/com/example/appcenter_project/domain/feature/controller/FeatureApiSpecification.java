package com.example.appcenter_project.domain.feature.controller;

import com.example.appcenter_project.domain.feature.dto.request.RequestFeatureDto;
import com.example.appcenter_project.domain.feature.dto.response.ResponseFeatureDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Feature Flag API", description = "기능 플래그 관련 API")
public interface FeatureApiSpecification {

    @Operation(
            summary = "기능 플래그 생성",
            description = "새로운 기능 플래그를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "기능 플래그 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "409", description = "이미 존재하는 키입니다.")
            }
    )
    ResponseEntity<Void> saveFeature(
            @RequestBody
            @Parameter(description = "기능 플래그 정보 (key, flag)", required = true)
            RequestFeatureDto dto);

    @Operation(
            summary = "특정 기능 플래그 조회",
            description = "키를 통해 특정 기능 플래그를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "기능 플래그 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseFeatureDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 기능 플래그입니다.")
            }
    )
    ResponseEntity<ResponseFeatureDto> findFeature(
            @PathVariable
            @Parameter(description = "기능 플래그 키", required = true, example = "dark_mode")
            String key);

    @Operation(
            summary = "전체 기능 플래그 목록 조회",
            description = "등록된 모든 기능 플래그를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "기능 플래그 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseFeatureDto.class))
                            )
                    )
            }
    )
    ResponseEntity<List<ResponseFeatureDto>> findAllFeatures();

    @Operation(
            summary = "기능 플래그 수정",
            description = "기존 기능 플래그의 값을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "기능 플래그 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 기능 플래그입니다.")
            }
    )
    ResponseEntity<Void> updateFeature(
            @RequestBody
            @Parameter(description = "수정할 기능 플래그 정보", required = true)
            RequestFeatureDto dto);
}