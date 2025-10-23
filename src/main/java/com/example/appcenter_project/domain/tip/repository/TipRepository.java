package com.example.appcenter_project.domain.tip.repository;

import com.example.appcenter_project.domain.tip.entity.Tip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipRepository extends JpaRepository<Tip, Long> {
    Optional<Tip> findByIdAndUserId(Long id, Long userId);
    List<Tip> findAllByOrderByIdDesc();
    List<Tip> findByUserId(Long userId);
    List<Long> findAllTipIds();
    boolean existsByIdAndUserId(Long tipId, Long userId);
}
