package com.example.appcenter_project.repository.tip;

import com.example.appcenter_project.entity.tip.TipComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipCommentRepository extends JpaRepository<TipComment, Long> {
    List<TipComment> findByTip_IdAndParentTipCommentIsNull(Long tipId);
    List<TipComment> findByTip_Id(Long tipId);
    Optional<TipComment> findByIdAndUserId(Long id, Long userId);
}
