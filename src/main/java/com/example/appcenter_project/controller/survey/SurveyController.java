package com.example.appcenter_project.controller.survey;

import com.example.appcenter_project.dto.request.survey.RequestSurveyDto;
import com.example.appcenter_project.dto.request.survey.RequestSurveyResponseDto;
import com.example.appcenter_project.dto.response.survey.ResponseSurveyDetailDto;
import com.example.appcenter_project.dto.response.survey.ResponseSurveyDto;
import com.example.appcenter_project.dto.response.survey.ResponseSurveyResultDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.survey.SurveyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/surveys")
public class SurveyController implements SurveyApiSpecification {

    private final SurveyService surveyService;

    // 설문 생성 (관리자)
    @PostMapping
    public ResponseEntity<Long> createSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RequestSurveyDto requestDto) {
        Long surveyId = surveyService.createSurvey(user.getId(), requestDto);
        return ResponseEntity.status(CREATED).body(surveyId);
    }

    // 모든 설문 조회
    @GetMapping
    public ResponseEntity<List<ResponseSurveyDto>> getAllSurveys() {
        return ResponseEntity.status(OK).body(surveyService.getAllSurveys());
    }

    // 설문 상세 조회
    @GetMapping("/{surveyId}")
    public ResponseEntity<ResponseSurveyDetailDto> getSurveyDetail(
            @PathVariable Long surveyId) {
        return ResponseEntity.status(OK).body(surveyService.getSurveyDetail(surveyId));
    }

    // 설문 수정 (관리자)
    @PutMapping("/{surveyId}")
    public ResponseEntity<Void> updateSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long surveyId,
            @Valid @RequestBody RequestSurveyDto requestDto) {
        surveyService.updateSurvey(user.getId(), surveyId, requestDto);
        return ResponseEntity.status(OK).build();
    }

    // 설문 삭제 (관리자)
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<Void> deleteSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long surveyId) {
        surveyService.deleteSurvey(user.getId(), surveyId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    // 설문 종료 (관리자)
    @PatchMapping("/{surveyId}/close")
    public ResponseEntity<Void> closeSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long surveyId) {
        surveyService.closeSurvey(user.getId(), surveyId);
        return ResponseEntity.status(OK).build();
    }

    // 설문 답변 제출 (사용자)
    @PostMapping("/responses")
    public ResponseEntity<Long> submitSurveyResponse(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RequestSurveyResponseDto requestDto) {
        Long responseId = surveyService.submitSurveyResponse(user.getId(), requestDto);
        return ResponseEntity.status(CREATED).body(responseId);
    }

    // 설문 결과/통계 조회 (관리자)
    @GetMapping("/{surveyId}/results")
    public ResponseEntity<ResponseSurveyResultDto> getSurveyResults(
            @PathVariable Long surveyId) {
        return ResponseEntity.status(OK).body(surveyService.getSurveyResults(surveyId));
    }
}

