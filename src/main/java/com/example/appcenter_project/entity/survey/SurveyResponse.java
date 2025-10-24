package com.example.appcenter_project.entity.survey;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 설문 응답 엔티티
 * 한 사용자가 한 설문에 대해 제출한 전체 응답 (제출 단위)
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SurveyResponse extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 응답 ID (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;  // 응답한 설문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 응답한 사용자

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyAnswer> answers = new ArrayList<>();  // 각 질문에 대한 답변들 (질문 수만큼 답변 존재)

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public void addAnswer(SurveyAnswer answer) {
        this.answers.add(answer);
        answer.setResponse(this);
    }
}

