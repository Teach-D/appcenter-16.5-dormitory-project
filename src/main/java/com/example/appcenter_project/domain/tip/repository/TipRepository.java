package com.example.appcenter_project.domain.tip.repository;

import com.example.appcenter_project.domain.tip.entity.Tip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TipRepository extends JpaRepository<Tip, Long> {
    Optional<Tip> findByIdAndUserId(Long id, Long userId);
    List<Tip> findAllByOrderByIdDesc();
    List<Tip> findByUserId(Long userId);

    @Query("SELECT t.id FROM Tip t")
    List<Long> findAllTipIds();
    boolean existsByIdAndUserId(Long tipId, Long userId);
}