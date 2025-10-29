package com.example.appcenter_project.controller.survey;

import com.example.appcenter_project.dto.request.survey.RequestSurveyDto;
import com.example.appcenter_project.dto.request.survey.RequestSurveyResponseDto;
import com.example.appcenter_project.dto.response.survey.ResponseSurveyDetailDto;
import com.example.appcenter_project.dto.response.survey.ResponseSurveyDto;
import com.example.appcenter_project.dto.response.survey.ResponseSurveyResultDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Survey API", description = "설문조사 관련 API")
public interface SurveyApiSpecification {

    @Operation(
            summary = "설문 생성 (관리자)",
            description = "관리자가 새로운 설문을 생성합니다. 객관식/주관식 질문과 선택지를 포함할 수 있습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문 생성 요청 DTO",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequestSurveyDto.class),
                            examples = @ExampleObject(
                                    name = "설문 생성 예시",
                                    summary = "기숙사 만족도 조사",
                                    value = """
                                            {
                                              "title": "2025년 기숙사 만족도 조사",
                                              "description": "익명으로 진행되며 약 5분 소요됩니다.",
                                              "startDate": "2025-01-01T00:00:00",
                                              "endDate": "2025-12-31T23:59:59",
                                              "recruitmentCount": "100",
                                              "questions": [
                                                {
                                                  "questionText": "기숙사에 만족하시나요?",
                                                  "questionType": "MULTIPLE_CHOICE",
                                                  "questionOrder": 1,
                                                  "isRequired": true,
                                                  "allowMultipleSelection": false,
                                                  "options": [
                                                    {
                                                      "optionText": "매우 만족",
                                                      "optionOrder": 1
                                                    },
                                                    {
                                                      "optionText": "만족",
                                                      "optionOrder": 2
                                                    },
                                                    {
                                                      "optionText": "보통",
                                                      "optionOrder": 3
                                                    },
                                                    {
                                                      "optionText": "불만족",
                                                      "optionOrder": 4
                                                    }
                                                  ]
                                                },
                                                {
                                                  "questionText": "개선이 필요한 점을 자유롭게 작성해주세요.",
                                                  "questionType": "SHORT_ANSWER",
                                                  "questionOrder": 2,
                                                  "isRequired": false,
                                                  "allowMultipleSelection": false,
                                                  "options": []
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "설문 생성 성공",
                            content = @Content(schema = @Schema(type = "integer", format = "int64", description = "생성된 설문 ID"))
                    ),
                    @ApiResponse(responseCode = "400", description = "입력값이 유효하지 않습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.", content = @Content(examples = {}))
            }
    )
    ResponseEntity<Long> createSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RequestSurveyDto requestDto);

    @Operation(
            summary = "모든 설문 조회",
            description = "등록된 모든 설문 목록을 조회합니다. 종료일이 지난 설문은 자동으로 종료 처리됩니다. 로그인한 사용자의 경우 각 설문의 제출 여부(hasSubmitted)를 포함하여 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "설문 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseSurveyDto.class))
                            )
                    )
            }
    )
    ResponseEntity<List<ResponseSurveyDto>> getAllSurveys();


    @Operation(
            summary = "설문 상세 조회",
            description = "설문 ID로 설문의 모든 질문과 선택지를 포함한 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "설문 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseSurveyDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 설문입니다.", content = @Content(examples = {}))
            }
    )
    ResponseEntity<ResponseSurveyDetailDto> getSurveyDetail(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "설문 ID", required = true, example = "1") Long surveyId);

    @Operation(
            summary = "설문 수정 (관리자)",
            description = """
                    관리자가 설문 ID를 통해 설문 정보를 수정합니다. 설문 생성자만 수정 가능합니다.
                    
                    **질문 수정 방식:**
                    - questionId가 있으면: 기존 질문 업데이트 (기존 답변 유지)
                    - questionId가 없으면: 새 질문 추가
                    - 요청에 포함되지 않은 기존 질문: 삭제
                    
                    **옵션 수정 방식:**
                    - optionId가 있으면: 기존 옵션 업데이트
                    - optionId가 없으면: 새 옵션 추가
                    - 요청에 포함되지 않은 기존 옵션: 삭제
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문 수정 요청 DTO",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequestSurveyDto.class),
                            examples = @ExampleObject(
                                    name = "설문 수정 예시",
                                    summary = "기숙사 만족도 조사 수정 - 기존 질문 업데이트 + 새 질문 추가",
                                    value = """
                                            {
                                              "title": "2025년 기숙사 만족도 조사 (수정)",
                                              "description": "익명으로 진행되며 약 5분 소요됩니다.",
                                              "startDate": "2025-01-01T00:00:00",
                                              "endDate": "2025-12-31T23:59:59",
                                              "recruitmentCount": "100",
                                              "questions": [
                                                {
                                                  "questionId": 1,
                                                  "questionText": "기숙사 시설에 만족하시나요? (수정됨)",
                                                  "questionType": "MULTIPLE_CHOICE",
                                                  "questionOrder": 1,
                                                  "isRequired": true,
                                                  "allowMultipleSelection": false,
                                                  "options": [
                                                    {
                                                      "optionId": 1,
                                                      "optionText": "매우 만족",
                                                      "optionOrder": 1
                                                    },
                                                    {
                                                      "optionId": 2,
                                                      "optionText": "만족",
                                                      "optionOrder": 2
                                                    },
                                                    {
                                                      "optionId": 3,
                                                      "optionText": "보통",
                                                      "optionOrder": 3
                                                    },
                                                    {
                                                      "optionText": "불만족 (새로 추가됨)",
                                                      "optionOrder": 4
                                                    }
                                                  ]
                                                },
                                                {
                                                  "questionId": 2,
                                                  "questionText": "개선이 필요한 점을 자유롭게 작성해주세요.",
                                                  "questionType": "SHORT_ANSWER",
                                                  "questionOrder": 2,
                                                  "isRequired": false,
                                                  "allowMultipleSelection": false,
                                                  "options": []
                                                },
                                                {
                                                  "questionText": "식당 음식은 어떠신가요? (새로 추가된 질문)",
                                                  "questionType": "MULTIPLE_CHOICE",
                                                  "questionOrder": 3,
                                                  "isRequired": false,
                                                  "allowMultipleSelection": true,
                                                  "options": [
                                                    {
                                                      "optionText": "맛있다",
                                                      "optionOrder": 1
                                                    },
                                                    {
                                                      "optionText": "양이 적절하다",
                                                      "optionOrder": 2
                                                    },
                                                    {
                                                      "optionText": "메뉴가 다양하다",
                                                      "optionOrder": 3
                                                    }
                                                  ]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "설문 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력값이 유효하지 않습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "수정 권한이 없습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 설문입니다.", content = @Content(examples = {}))
            }
    )
    ResponseEntity<Void> updateSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "설문 ID", required = true, example = "1") Long surveyId,
            @Valid @RequestBody RequestSurveyDto requestDto);

    @Operation(
            summary = "설문 삭제 (관리자)",
            description = "관리자가 설문 ID를 통해 설문을 삭제합니다. 설문 생성자만 삭제 가능하며, 관련 질문, 선택지, 응답도 모두 삭제됩니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "설문 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "삭제 권한이 없습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 설문입니다.", content = @Content(examples = {}))
            }
    )
    ResponseEntity<Void> deleteSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "설문 ID", required = true, example = "1") Long surveyId);

    @Operation(
            summary = "설문 종료 (관리자)",
            description = "관리자가 설문을 종료합니다. 종료된 설문은 더 이상 응답을 받지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "설문 종료 성공"),
                    @ApiResponse(responseCode = "403", description = "종료 권한이 없습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 설문입니다.", content = @Content(examples = {}))
            }
    )
    ResponseEntity<Void> closeSurvey(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "설문 ID", required = true, example = "1") Long surveyId);

    @Operation(
            summary = "설문 답변 제출 (사용자)",
            description = "로그인한 사용자가 설문에 답변을 제출합니다. 한 설문에 한 번만 응답 가능합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "설문 응답 제출 DTO",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequestSurveyResponseDto.class),
                            examples = @ExampleObject(
                                    name = "설문 응답 제출 예시",
                                    summary = "기숙사 만족도 조사 응답",
                                    value = """
                                            {
                                              "surveyId": 1,
                                              "answers": [
                                                {
                                                  "questionId": 1,
                                                  "optionIds": [2],
                                                  "answerText": null
                                                },
                                                {
                                                  "questionId": 2,
                                                  "optionIds": [],
                                                  "answerText": "조식 메뉴가 다양했으면 좋겠습니다."
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "설문 응답 제출 성공",
                            content = @Content(schema = @Schema(type = "integer", format = "int64", description = "생성된 응답 ID"))
                    ),
                    @ApiResponse(responseCode = "400", description = "입력값이 유효하지 않거나 이미 응답한 설문입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "설문을 찾을 수 없습니다.", content = @Content(examples = {}))
            }
    )
    ResponseEntity<Long> submitSurveyResponse(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RequestSurveyResponseDto requestDto);

    @Operation(
            summary = "설문 결과/통계 조회 (관리자)",
            description = "관리자가 설문의 응답 결과와 통계를 조회합니다. 객관식은 선택지별 횟수와 비율, 주관식은 모든 답변 텍스트를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "설문 결과 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseSurveyResultDto.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 설문입니다.", content = @Content(examples = {}))
            }
    )
    ResponseEntity<ResponseSurveyResultDto> getSurveyResults(
            @PathVariable
            @Parameter(description = "설문 ID", required = true, example = "1") Long surveyId);

}

