package com.example.appcenter_project.repository.survey;

import com.example.appcenter_project.entity.survey.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {

    List<SurveyOption> findByQuestionIdOrderByOptionOrderAsc(Long questionId);
}

