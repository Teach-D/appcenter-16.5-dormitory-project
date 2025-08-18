package com.example.appcenter_project.repository.tip;

import com.example.appcenter_project.entity.tip.Tip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipRepository extends JpaRepository<Tip, Long> {
    Optional<Tip> findByIdAndUserId(Long id, Long userId);
    List<Tip> findByUserId(Long userId);
}
