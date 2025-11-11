package com.example.appcenter_project.domain.survey.repository;

import com.example.appcenter_project.domain.survey.entiity.Survey;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    List<Survey> findByCreatorId(Long creatorId);

    @Query("SELECT s FROM Survey s WHERE s.status = 'PROCEEDING'")
    List<Survey> findActiveSurveys();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT s FROM Survey s LEFT JOIN FETCH s.questions WHERE s.id = :id")
    Optional<Survey> findByIdWithQuestions(@Param("id") Long id);
}

