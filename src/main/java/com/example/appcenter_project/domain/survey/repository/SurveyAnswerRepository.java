package com.example.appcenter_project.domain.survey.repository;

import com.example.appcenter_project.domain.survey.entiity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    List<SurveyAnswer> findByQuestionId(Long questionId);
}

