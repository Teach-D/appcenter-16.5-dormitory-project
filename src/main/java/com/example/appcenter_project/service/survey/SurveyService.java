package com.example.appcenter_project.service.survey;

import com.example.appcenter_project.dto.request.survey.*;
import com.example.appcenter_project.dto.response.survey.*;
import com.example.appcenter_project.entity.survey.*;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.survey.QuestionType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.survey.*;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.CustomUserDetails;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final UserRepository userRepository;

    // 설문 생성 (관리자)
    public Long createSurvey(Long userId, RequestSurveyDto requestDto) {
        log.info("[createSurvey] userId={}가 설문 생성 요청", userId);

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 시작일시와 종료일시에서 9시간 빼기
        LocalDateTime adjustedStartDate = requestDto.getStartDate().plusHours(9);
        LocalDateTime adjustedEndDate = requestDto.getEndDate().plusHours(9);

        Survey survey = Survey.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .creator(creator)
                .startDate(adjustedStartDate)
                .endDate(adjustedEndDate)
                .recruitmentCount(requestDto.getRecruitmentCount())
                .build();

        // 초기 상태 설정
        survey.updateStatus();

        surveyRepository.save(survey);
        log.info("[createSurvey] 설문 저장 완료 surveyId={}", survey.getId());

        // 질문 저장
        for (RequestSurveyQuestionDto questionDto : requestDto.getQuestions()) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .questionText(questionDto.getQuestionText())
                    .questionType(questionDto.getQuestionType())
                    .questionOrder(questionDto.getQuestionOrder())
                    .isRequired(questionDto.isRequired())
                    .allowMultipleSelection(questionDto.isAllowMultipleSelection())
                    .build();

            survey.addQuestion(question);
            surveyQuestionRepository.save(question);

            // 객관식인 경우 선택지 저장
            if (questionDto.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                for (RequestSurveyOptionDto optionDto : questionDto.getOptions()) {
                    SurveyOption option = SurveyOption.builder()
                            .optionText(optionDto.getOptionText())
                            .optionOrder(optionDto.getOptionOrder())
                            .build();
                    question.addOption(option);
                    surveyOptionRepository.save(option);
                }
            }
        }

        log.info("[createSurvey] 설문 생성 완료 surveyId={}", survey.getId());
        return survey.getId();
    }

    // 모든 설문 조회
    @Transactional
    public List<ResponseSurveyDto> getAllSurveys() {
        log.info("[getAllSurveys] 모든 설문 조회");

        List<Survey> surveys = surveyRepository.findAll();

        // 설문 상태 자동 업데이트
        for (Survey survey : surveys) {
            survey.updateStatus();
        }

        // SecurityContext에서 userId 가져오기
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getId();
        }

        // 각 설문에 대해 사용자 제출 여부 확인
        List<ResponseSurveyDto> result = new ArrayList<>();
        for (Survey survey : surveys) {
            boolean hasSubmitted = false;
            if (userId != null) {
                hasSubmitted = surveyResponseRepository.existsBySurveyIdAndUserId(survey.getId(), userId);
            }
            result.add(ResponseSurveyDto.entityToDto(survey, hasSubmitted));
        }

        return result;
    }

    // 설문 상세 조회
    @Transactional
    public ResponseSurveyDetailDto getSurveyDetail(Long userId, Long surveyId) {
        log.info("[getSurveyDetail] surveyId={} 조회", surveyId);

        Survey survey = surveyRepository.findByIdWithQuestions(surveyId)
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        // 조회 시점에 상태 자동 업데이트
        survey.updateStatus();

        boolean hasSubmitted = surveyResponseRepository.existsBySurveyIdAndUserId(survey.getId(), userId);

        return ResponseSurveyDetailDto.entityToDto(survey, hasSubmitted);
    }

    // 설문 수정 (관리자)
    public void updateSurvey(Long userId, Long surveyId, RequestSurveyDto requestDto) {
        log.info("[updateSurvey] userId={}가 surveyId={} 수정 요청", userId, surveyId);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        // 생성자 확인
        if (!survey.getCreator().getId().equals(userId)) {
            throw new CustomException(SURVEY_NOT_OWNED_BY_USER);
        }

        // 설문 기본 정보 업데이트
        survey.update(requestDto.getTitle(), requestDto.getDescription(),
                requestDto.getStartDate().plusHours(9), requestDto.getEndDate().plusHours(9), requestDto.getRecruitmentCount());

        // 기존 질문들을 ID로 매핑
        Map<Long, SurveyQuestion> existingQuestionsMap = survey.getQuestions().stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, q -> q));

        // 요청에 포함된 질문 ID들을 추적
        Set<Long> requestQuestionIds = new HashSet<>();

        // 새로 추가할 질문들을 임시로 저장
        List<SurveyQuestion> newQuestions = new ArrayList<>();

        // 질문 처리: 업데이트 또는 생성
        for (RequestSurveyQuestionDto questionDto : requestDto.getQuestions()) {
            if (questionDto.getQuestionId() != null) {
                // 기존 질문 업데이트
                requestQuestionIds.add(questionDto.getQuestionId());
                SurveyQuestion existingQuestion = existingQuestionsMap.get(questionDto.getQuestionId());

                if (existingQuestion == null) {
                    throw new CustomException(SURVEY_QUESTION_NOT_FOUND);
                }

                log.info("[updateSurvey] 기존 질문 업데이트: questionId={}", questionDto.getQuestionId());

                // 질문 내용 업데이트
                existingQuestion.update(
                        questionDto.getQuestionText(),
                        questionDto.getQuestionType(),
                        questionDto.getQuestionOrder(),
                        questionDto.isRequired(),
                        questionDto.isAllowMultipleSelection()
                );

                // 객관식인 경우 옵션 처리
                if (questionDto.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                    updateQuestionOptions(existingQuestion, questionDto.getOptions());
                } else {
                    // 주관식으로 변경된 경우 모든 옵션 제거
                    existingQuestion.getOptions().clear();
                }

            } else {
                // 새 질문 추가
                log.info("[updateSurvey] 새 질문 추가: {}", questionDto.getQuestionText());

                SurveyQuestion newQuestion = SurveyQuestion.builder()
                        .questionText(questionDto.getQuestionText())
                        .questionType(questionDto.getQuestionType())
                        .questionOrder(questionDto.getQuestionOrder())
                        .isRequired(questionDto.isRequired())
                        .allowMultipleSelection(questionDto.isAllowMultipleSelection())
                        .build();

                // 객관식인 경우 선택지 저장
                if (questionDto.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                    for (RequestSurveyOptionDto optionDto : questionDto.getOptions()) {
                        SurveyOption option = SurveyOption.builder()
                                .optionText(optionDto.getOptionText())
                                .optionOrder(optionDto.getOptionOrder())
                                .build();
                        newQuestion.addOption(option);
                    }
                }

                newQuestions.add(newQuestion);
            }
        }

        // 요청에 포함되지 않은 기존 질문들 삭제
        List<SurveyQuestion> questionsToRemove = survey.getQuestions().stream()
                .filter(q -> !requestQuestionIds.contains(q.getId()))
                .collect(Collectors.toList());

        if (!questionsToRemove.isEmpty()) {
            log.info("[updateSurvey] 삭제할 질문 수: {}", questionsToRemove.size());
            for (SurveyQuestion questionToRemove : questionsToRemove) {
                log.info("[updateSurvey] 질문 삭제: questionId={}", questionToRemove.getId());
                survey.getQuestions().remove(questionToRemove);
            }
        }

        // 새 질문들을 설문에 추가
        for (SurveyQuestion newQuestion : newQuestions) {
            survey.addQuestion(newQuestion);
            log.info("[updateSurvey] 새 질문 추가 완료: {}", newQuestion.getQuestionText());
        }

        surveyRepository.save(survey); // CascadeType.ALL로 인해 질문과 옵션도 함께 저장/업데이트
        log.info("[updateSurvey] surveyId={} 수정 완료, 총 질문 수: {}", surveyId, survey.getQuestions().size());
    }

    // 질문의 옵션들 업데이트
    private void updateQuestionOptions(SurveyQuestion question, List<RequestSurveyOptionDto> optionDtos) {
        // 기존 옵션들을 ID로 매핑
        Map<Long, SurveyOption> existingOptionsMap = question.getOptions().stream()
                .collect(Collectors.toMap(SurveyOption::getId, o -> o));

        // 요청에 포함된 옵션 ID들을 추적
        Set<Long> requestOptionIds = new HashSet<>();

        // 옵션 처리: 업데이트 또는 생성
        for (RequestSurveyOptionDto optionDto : optionDtos) {
            if (optionDto.getOptionId() != null) {
                // 기존 옵션 업데이트
                requestOptionIds.add(optionDto.getOptionId());
                SurveyOption existingOption = existingOptionsMap.get(optionDto.getOptionId());

                if (existingOption != null) {
                    existingOption.update(optionDto.getOptionText(), optionDto.getOptionOrder());
                }
            } else {
                // 새 옵션 추가
                SurveyOption newOption = SurveyOption.builder()
                        .optionText(optionDto.getOptionText())
                        .optionOrder(optionDto.getOptionOrder())
                        .build();
                question.addOption(newOption);
            }
        }

        // 요청에 포함되지 않은 기존 옵션들 삭제
        List<SurveyOption> optionsToRemove = question.getOptions().stream()
                .filter(o -> !requestOptionIds.contains(o.getId()))
                .collect(Collectors.toList());

        for (SurveyOption optionToRemove : optionsToRemove) {
            question.getOptions().remove(optionToRemove);
        }
    }

    // 설문 삭제 (관리자)
    public void deleteSurvey(Long userId, Long surveyId) {
        log.info("[deleteSurvey] userId={}가 surveyId={} 삭제 요청", userId, surveyId);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        // 생성자 확인
        if (!survey.getCreator().getId().equals(userId)) {
            throw new CustomException(SURVEY_NOT_OWNED_BY_USER);
        }

        surveyRepository.delete(survey);
        log.info("[deleteSurvey] surveyId={} 삭제 완료", surveyId);
    }

    // 설문 종료 (관리자)
    public void closeSurvey(Long userId, Long surveyId) {
        log.info("[closeSurvey] userId={}가 surveyId={} 종료 요청", userId, surveyId);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        // 생성자 확인
        if (!survey.getCreator().getId().equals(userId)) {
            throw new CustomException(SURVEY_NOT_OWNED_BY_USER);
        }

        survey.close();
        log.info("[closeSurvey] surveyId={} 종료 완료", surveyId);
    }

    // 설문 답변 제출 (사용자)
    public Long submitSurveyResponse(Long userId, RequestSurveyResponseDto requestDto) {
        log.info("[submitSurveyResponse] userId={}가 surveyId={} 응답 제출", userId, requestDto.getSurveyId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        Survey survey = surveyRepository.findByIdWithQuestions(requestDto.getSurveyId())
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        // 설문 종료 여부 확인
        if (survey.isClosed()) {
            throw new CustomException(SURVEY_CLOSED);
        }

        // 설문 기간 확인
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(survey.getStartDate()) || now.isAfter(survey.getEndDate())) {
            throw new CustomException(SURVEY_NOT_IN_PERIOD);
        }

        // 중복 응답 확인
        if (surveyResponseRepository.existsBySurveyIdAndUserId(requestDto.getSurveyId(), userId)) {
            throw new CustomException(ALREADY_SURVEY_RESPONSE);
        }

        if (survey.isMaxRecruitmentCount()) {
            survey.updateClosedStatus();
            throw new CustomException(RECRUITMENT_COUNT_MAX);
        }

        // 응답 생성
        SurveyResponse response = SurveyResponse.builder()
                .survey(survey)
                .user(user)
                .build();

        surveyResponseRepository.save(response);

        // 각 질문에 대한 답변 저장
        for (RequestSurveyAnswerDto answerDto : requestDto.getAnswers()) {
            SurveyQuestion question = surveyQuestionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new CustomException(SURVEY_QUESTION_NOT_FOUND));

            SurveyAnswer answer = SurveyAnswer.builder()
                    .question(question)
                    .build();

            // 객관식인 경우
            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                // 다중 선택 허용 여부 확인
                if (!question.isAllowMultipleSelection() && answerDto.getOptionIds().size() > 1) {
                    throw new CustomException(MULTIPLE_SELECTION_NOT_ALLOWED);
                }

                // 선택된 옵션들 추가
                for (Long optionId : answerDto.getOptionIds()) {
                    SurveyOption option = surveyOptionRepository.findById(optionId)
                            .orElseThrow(() -> new CustomException(SURVEY_OPTION_NOT_FOUND));
                    answer.addSelectedOption(option);
                }
            }
            // 주관식인 경우
            else if (question.getQuestionType() == QuestionType.SHORT_ANSWER) {
                answer = SurveyAnswer.builder()
                        .question(question)
                        .answerText(answerDto.getAnswerText())
                        .build();
            }

            response.addAnswer(answer);
            surveyAnswerRepository.save(answer);
        }

        survey.addResponse(response);
        log.info("[submitSurveyResponse] userId={}의 surveyId={} 응답 제출 완료", userId, requestDto.getSurveyId());
        return response.getId();
    }

    // 설문 결과/통계 조회 (관리자)
    @Transactional
    public ResponseSurveyResultDto getSurveyResults(Long surveyId) {
        log.info("[getSurveyResults] surveyId={} 결과 조회", surveyId);

        Survey survey = surveyRepository.findByIdWithQuestions(surveyId)
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(surveyId);
        int totalResponses = responses.size();

        List<ResponseSurveyResultDto.QuestionResultDto> questionResults = new ArrayList<>();

        for (SurveyQuestion question : survey.getQuestions()) {
            List<SurveyAnswer> answers = surveyAnswerRepository.findByQuestionId(question.getId());

            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                // 객관식 - 각 옵션별 선택 횟수 계산
                Map<Long, Integer> optionCountMap = new HashMap<>();
                for (SurveyOption option : question.getOptions()) {
                    optionCountMap.put(option.getId(), 0);
                }

                // 답변 카운팅 (다중 선택 고려)
                for (SurveyAnswer answer : answers) {
                    for (SurveyOption selectedOption : answer.getSelectedOptions()) {
                        optionCountMap.put(selectedOption.getId(),
                                optionCountMap.getOrDefault(selectedOption.getId(), 0) + 1);
                    }
                }

                // OptionResult 생성
                List<ResponseSurveyResultDto.OptionResultDto> optionResults = new ArrayList<>();
                for (SurveyOption option : question.getOptions()) {
                    int count = optionCountMap.get(option.getId());
                    double percentage = totalResponses > 0 ? (count * 100.0 / totalResponses) : 0.0;

                    optionResults.add(ResponseSurveyResultDto.OptionResultDto.builder()
                            .optionId(option.getId())
                            .optionText(option.getOptionText())
                            .count(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0) // 소수점 2자리
                            .build());
                }

                questionResults.add(ResponseSurveyResultDto.QuestionResultDto.builder()
                        .questionId(question.getId())
                        .questionText(question.getQuestionText())
                        .questionType(question.getQuestionType().name())
                        .optionResults(optionResults)
                        .build());

            } else if (question.getQuestionType() == QuestionType.SHORT_ANSWER) {
                // 주관식 - 모든 답변 텍스트 수집
                List<String> shortAnswers = answers.stream()
                        .map(SurveyAnswer::getAnswerText)
                        .filter(text -> text != null && !text.isEmpty())
                        .collect(Collectors.toList());

                questionResults.add(ResponseSurveyResultDto.QuestionResultDto.builder()
                        .questionId(question.getId())
                        .questionText(question.getQuestionText())
                        .questionType(question.getQuestionType().name())
                        .shortAnswers(shortAnswers)
                        .build());
            }
        }

        return ResponseSurveyResultDto.builder()
                .surveyId(survey.getId())
                .surveyTitle(survey.getTitle())
                .totalResponses(totalResponses)
                .questionResults(questionResults)
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .build();
    }

    // 설문 답변 CSV 추출 (관리자)
    @Transactional(readOnly = true)
    public byte[] exportSurveyToCsv(Long surveyId) {
        log.info("[exportSurveyToCsv] surveyId={} CSV 추출 시작", surveyId);

        Survey survey = surveyRepository.findByIdWithQuestions(surveyId)
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        List<SurveyResponse> responses = surveyResponseRepository.findBySurveyId(surveyId);

        // 질문 목록을 순서대로 정렬
        List<SurveyQuestion> sortedQuestions = survey.getQuestions().stream()
                .sorted(Comparator.comparing(SurveyQuestion::getQuestionOrder))
                .collect(Collectors.toList());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '"', "\r\n")) {

            // BOM 추가 (한글 Excel 호환성을 위해)
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);

            // CSV 헤더 작성
            List<String> headerList = new ArrayList<>();
            headerList.add("순서");
            headerList.add("학번");
            for (SurveyQuestion question : sortedQuestions) {
                headerList.add(question.getQuestionText());
            }
            String[] header = headerList.toArray(new String[0]);
            csvWriter.writeNext(header);

            // 각 응답에 대해 행 작성
            int rowNumber = 1;
            for (SurveyResponse response : responses) {
                List<String> rowList = new ArrayList<>();
                rowList.add(String.valueOf(rowNumber++));
                rowList.add(response.getUser().getStudentNumber() != null ? response.getUser().getStudentNumber() : "");

                // 각 질문에 대한 답변 찾기
                for (SurveyQuestion question : sortedQuestions) {
                    String answerText = getAnswerText(response, question);
                    rowList.add(answerText);
                }

                String[] row = rowList.toArray(new String[0]);
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            log.info("[exportSurveyToCsv] surveyId={} CSV 추출 완료, 총 {}개 응답", surveyId, responses.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("[exportSurveyToCsv] CSV 생성 중 오류 발생", e);
            throw new RuntimeException("CSV 파일 생성에 실패했습니다.", e);
        }
    }

    // 특정 질문에 대한 답변 텍스트 가져오기
    private String getAnswerText(SurveyResponse response, SurveyQuestion question) {
        SurveyAnswer answer = response.getAnswers().stream()
                .filter(a -> a.getQuestion().getId().equals(question.getId()))
                .findFirst()
                .orElse(null);

        if (answer == null) {
            return "";
        }

        // 객관식인 경우
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            if (answer.getSelectedOptions().isEmpty()) {
                return "";
            }
            // 선택된 옵션들을 ", "로 구분하여 연결
            return answer.getSelectedOptions().stream()
                    .sorted(Comparator.comparing(SurveyOption::getOptionOrder))
                    .map(SurveyOption::getOptionText)
                    .collect(Collectors.joining(", "));
        }
        // 주관식인 경우
        else if (question.getQuestionType() == QuestionType.SHORT_ANSWER) {
            return answer.getAnswerText() != null ? answer.getAnswerText() : "";
        }

        return "";
    }
}

