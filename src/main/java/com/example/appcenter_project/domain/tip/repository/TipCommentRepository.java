package com.example.appcenter_project.domain.tip.repository;

import com.example.appcenter_project.domain.tip.entity.TipComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipCommentRepository extends JpaRepository<TipComment, Long> {
    Optional<TipComment> findByIdAndUserId(Long id, Long userId);
    List<TipComment> findByTipIdAndParentTipCommentIsNull(Long tipId);

}
