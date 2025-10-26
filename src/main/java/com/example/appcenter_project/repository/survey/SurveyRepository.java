package com.example.appcenter_project.repository.survey;

import com.example.appcenter_project.entity.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findByCreatorId(Long creatorId);

    @Query("SELECT s FROM Survey s WHERE s.status = 'PROCEEDING'")
    List<Survey> findActiveSurveys();

    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.id = :id")
    Optional<Survey> findByIdWithQuestions(@Param("id") Long id);
}

