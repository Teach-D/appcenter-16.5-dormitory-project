package com.example.appcenter_project.entity.survey;

import com.example.appcenter_project.entity.BaseTimeEntity;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.survey.SurveyStatus;
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

    @Lob
    private String title;  // 설문 제목 (예: "2025년 기숙사 만족도 조사")

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;  // 설문 설명 (예: "익명으로 진행되며 5분 소요됩니다")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;  // 설문 생성자 (관리자)

    private LocalDateTime startDate;  // 설문 시작일시 (이 시간부터 응답 가능)

    private LocalDateTime endDate;  // 설문 종료일시 (이 시간 이후 자동 종료)

    private int recruitmentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SurveyStatus status = SurveyStatus.BEFORE;  // 설문 진행 상태

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

    public boolean isMaxRecruitmentCount() {
        if (recruitmentCount < 1) {
            return false;
        }

        if (responses.size() >= recruitmentCount) {
            return true;
        }

        return false;
    }

    public void addResponse(SurveyResponse response) {
        this.responses.add(response);
    }

    public void close() {
        this.status = SurveyStatus.CLOSED;
    }

    public void reopen() {
        updateStatus();
    }
    
    public void updateStatus() {
        // 관리자가 수동으로 종료한 경우는 날짜로 다시 활성화하지 않음
        if (this.status == SurveyStatus.CLOSED) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) {
            this.status = SurveyStatus.BEFORE;
        } else if (now.isAfter(endDate)) {
            this.status = SurveyStatus.CLOSED;
        } else {
            this.status = SurveyStatus.PROCEEDING;
        }
    }
    
    public boolean isClosed() {
        return status == SurveyStatus.CLOSED;
    }

    public void update(String title, String description, LocalDateTime startDate, LocalDateTime endDate, int recruitmentCount) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recruitmentCount = recruitmentCount;
        updateStatus();
    }

    public void updateClosedStatus() {
        this.status = SurveyStatus.CLOSED;
    }
}

