package com.example.appcenter_project.entity.survey;

import com.example.appcenter_project.enums.survey.QuestionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 설문 질문 엔티티
 * 설문 내의 각 질문 (객관식 또는 주관식)
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 질문 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;  // 이 질문이 속한 설문

    @Column(nullable = false)
    private String questionText;  // 질문 내용 (예: "기숙사에 만족하시나요?")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;  // 질문 타입 (MULTIPLE_CHOICE: 객관식, SHORT_ANSWER: 주관식)

    @Column(nullable = false)
    private Integer questionOrder;  // 질문 순서 (1, 2, 3...) - 설문에서 표시될 순서

    @Builder.Default
    private boolean isRequired = false;  // 필수 응답 여부 (true: 반드시 답변해야 함, false: 선택 사항)

    @Builder.Default
    private boolean allowMultipleSelection = false;  // 객관식일 경우 다중 선택 허용 여부 (true: 체크박스(복수선택), false: 라디오버튼(단일선택))

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyOption> options = new ArrayList<>();  // 객관식 선택지들 (주관식이면 비어있음)

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyAnswer> answers = new ArrayList<>();  // 이 질문에 대한 모든 사용자 답변들

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public void addOption(SurveyOption option) {
        this.options.add(option);
        option.setQuestion(this);
    }

    public void addAnswer(SurveyAnswer answer) {
        this.answers.add(answer);
    }

    public void update(String questionText, QuestionType questionType, Integer questionOrder, boolean isRequired, boolean allowMultipleSelection) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.questionOrder = questionOrder;
        this.isRequired = isRequired;
        this.allowMultipleSelection = allowMultipleSelection;
    }
}

