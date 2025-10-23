package com.example.appcenter_project.entity.survey;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 설문 엔티티
 * 관리자가 생성하는 설문의 기본 정보를 담는 엔티티
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Survey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 설문 ID (PK)

    @Column(nullable = false)
    private String title;  // 설문 제목 (예: "2025년 기숙사 만족도 조사")

    @Column(columnDefinition = "TEXT")
    private String description;  // 설문 설명 (예: "익명으로 진행되며 5분 소요됩니다")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;  // 설문 생성자 (관리자)

    private LocalDateTime startDate;  // 설문 시작일시 (이 시간부터 응답 가능)

    private LocalDateTime endDate;  // 설문 종료일시 (이 시간 이후 자동 종료)

    @Builder.Default
    private boolean isClosed = false;  // 설문 종료 여부 (true: 종료됨, false: 진행중) - 관리자가 수동으로도 종료 가능

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyQuestion> questions = new ArrayList<>();  // 이 설문에 속한 모든 질문들 (객관식/주관식)

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyResponse> responses = new ArrayList<>();  // 이 설문에 대한 모든 사용자 응답들

    public void addQuestion(SurveyQuestion question) {
        this.questions.add(question);
        question.setSurvey(this);
    }

    public void addResponse(SurveyResponse response) {
        this.responses.add(response);
    }

    public void close() {
        this.isClosed = true;
    }

    public void reopen() {
        this.isClosed = false;
    }

    public void update(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

