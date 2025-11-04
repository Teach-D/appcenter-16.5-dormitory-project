package com.example.appcenter_project.domain.survey.repository;

import com.example.appcenter_project.domain.survey.entiity.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {

    List<SurveyOption> findByQuestionIdOrderByOptionOrderAsc(Long questionId);
}

