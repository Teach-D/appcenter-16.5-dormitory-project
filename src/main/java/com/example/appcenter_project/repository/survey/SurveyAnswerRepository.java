package com.example.appcenter_project.repository.survey;

import com.example.appcenter_project.entity.survey.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    List<SurveyAnswer> findByQuestionId(Long questionId);
}

