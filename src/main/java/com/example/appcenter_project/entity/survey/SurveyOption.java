package com.example.appcenter_project.entity.survey;

import jakarta.persistence.*;
import lombok.*;

/**
 * 객관식 선택지 엔티티
 * 객관식 질문의 각 보기 (①, ②, ③ 등)
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SurveyOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 선택지 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;  // 이 선택지가 속한 질문

    @Column(nullable = false)
    private String optionText;  // 선택지 내용 (예: "매우 만족", "만족", "보통", "불만족")

    @Column(nullable = false)
    private Integer optionOrder;  // 선택지 순서 (1, 2, 3...) - 보기에서 표시될 순서

    public void setQuestion(SurveyQuestion question) {
        this.question = question;
    }

    public void update(String optionText, Integer optionOrder) {
        this.optionText = optionText;
        this.optionOrder = optionOrder;
    }
}

