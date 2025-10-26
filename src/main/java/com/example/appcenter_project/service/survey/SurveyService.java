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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Survey survey = Survey.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .creator(creator)
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .isClosed(false) // 초기에는 종료되지 않은 상태
                .build();

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
    @Transactional(readOnly = true)
    public List<ResponseSurveyDto> getAllSurveys() {
        log.info("[getAllSurveys] 모든 설문 조회");

        List<Survey> surveys = surveyRepository.findAll();

        // 종료일이 지난 설문 자동 종료 처리
        LocalDateTime now = LocalDateTime.now();
        for (Survey survey : surveys) {
            if (!survey.isClosed() && now.isAfter(survey.getEndDate())) {
                survey.close();
            }
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
    @Transactional(readOnly = true)
    public ResponseSurveyDetailDto getSurveyDetail(Long surveyId) {
        log.info("[getSurveyDetail] surveyId={} 조회", surveyId);

        Survey survey = surveyRepository.findByIdWithQuestions(surveyId)
                .orElseThrow(() -> new CustomException(SURVEY_NOT_FOUND));

        // 조회 시점에 종료일이 지났으면 자동 종료 처리
        LocalDateTime now = LocalDateTime.now();
        if (!survey.isClosed() && now.isAfter(survey.getEndDate())) {
            survey.close();
        }

        return ResponseSurveyDetailDto.entityToDto(survey);
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
                requestDto.getStartDate(), requestDto.getEndDate());

        // 기존 질문 삭제
        survey.getQuestions().clear();

        // 새 질문 추가
        for (RequestSurveyQuestionDto questionDto : requestDto.getQuestions()) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .questionText(questionDto.getQuestionText())
                    .questionType(questionDto.getQuestionType())
                    .questionOrder(questionDto.getQuestionOrder())
                    .isRequired(questionDto.isRequired())
                    .allowMultipleSelection(questionDto.isAllowMultipleSelection())
                    .build();

            survey.addQuestion(question);

            // 객관식인 경우 선택지 저장
            if (questionDto.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                for (RequestSurveyOptionDto optionDto : questionDto.getOptions()) {
                    SurveyOption option = SurveyOption.builder()
                            .optionText(optionDto.getOptionText())
                            .optionOrder(optionDto.getOptionOrder())
                            .build();
                    question.addOption(option);
                }
            }
        }
        surveyRepository.save(survey); // CascadeType.ALL로 인해 질문과 옵션도 함께 저장/업데이트
        log.info("[updateSurvey] surveyId={} 수정 완료", surveyId);
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
    @Transactional(readOnly = true)
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
                .build();
    }
}

