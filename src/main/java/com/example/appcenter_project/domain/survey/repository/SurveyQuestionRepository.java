package com.example.appcenter_project.domain.survey.repository;

import com.example.appcenter_project.domain.survey.entiity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    List<SurveyQuestion> findBySurveyIdOrderByQuestionOrderAsc(Long surveyId);
}

