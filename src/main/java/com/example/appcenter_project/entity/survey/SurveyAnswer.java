package com.example.appcenter_project.entity.survey;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 개별 질문 답변 엔티티
 * 하나의 질문에 대한 하나의 답변
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 답변 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponse response;  // 이 답변이 속한 응답 (제출 단위)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;  // 답변한 질문

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "survey_answer_options",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    @Builder.Default
    private List<SurveyOption> selectedOptions = new ArrayList<>();  // 객관식인 경우 선택된 옵션들 (단일선택: 1개, 다중선택: 여러개)

    @Lob
    private String answerText;  // 주관식인 경우 입력한 텍스트 답변 내용

    public void setResponse(SurveyResponse response) {
        this.response = response;
    }

    public void addSelectedOption(SurveyOption option) {
        this.selectedOptions.add(option);
    }
}

