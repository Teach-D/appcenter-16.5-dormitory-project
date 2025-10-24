package com.example.appcenter_project.repository.survey;

import com.example.appcenter_project.entity.survey.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    List<SurveyQuestion> findBySurveyIdOrderByQuestionOrderAsc(Long surveyId);
}

