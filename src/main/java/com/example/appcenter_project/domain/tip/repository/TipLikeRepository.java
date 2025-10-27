package com.example.appcenter_project.domain.tip.repository;

import com.example.appcenter_project.domain.tip.entity.TipLike;
import com.example.appcenter_project.domain.tip.entity.Tip;
import com.example.appcenter_project.domain.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TipLikeRepository extends JpaRepository<TipLike, Long> {
    Optional<TipLike> findByUserAndTip(User user, Tip tip);
    List<TipLike> findByUserId(Long userId);

    @Query("SELECT tl FROM TipLike tl JOIN FETCH tl.tip t WHERE tl.user.id = :userId")
    List<TipLike> findByUserIdWithTip(@Param("userId") Long userId);
}
