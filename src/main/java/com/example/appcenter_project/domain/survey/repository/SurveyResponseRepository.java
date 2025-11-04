package com.example.appcenter_project.domain.survey.repository;

import com.example.appcenter_project.domain.survey.entiity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    List<SurveyResponse> findByUserId(Long userId);

    List<SurveyResponse> findBySurveyId(Long surveyId);

    boolean existsBySurveyIdAndUserId(Long surveyId, Long userId);

    @Query("SELECT sr FROM SurveyResponse sr LEFT JOIN FETCH sr.answers WHERE sr.id = :id")
    Optional<SurveyResponse> findByIdWithAnswers(@Param("id") Long id);
}

