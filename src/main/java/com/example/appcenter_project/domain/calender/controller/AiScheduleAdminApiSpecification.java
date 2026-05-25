package com.example.appcenter_project.domain.calender.controller;

import com.example.appcenter_project.domain.calender.dto.response.ResponseFailedScheduleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "AI 캘린더 관리자", description = "AI 일정 추출 영구 실패 목록 조회 등 관리자 기능")
public interface AiScheduleAdminApiSpecification {

    @Operation(summary = "AI 일정 추출 영구 실패 목록 조회",
            description = "재시도 한도(3회)를 초과해 더 이상 자동 처리되지 않는 공지 목록을 페이징으로 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<ResponseFailedScheduleDto> getFailedList(int page, int size);

    @Operation(summary = "AI 일정 추출 수동 트리거",
            description = "PENDING/FAILED 상태 공지에 대한 AI 일정 추출을 즉시 실행 (비동기)")
    @ApiResponse(responseCode = "202", description = "트리거 수락 — 백그라운드 실행")
    ResponseEntity<Void> runNow();
}