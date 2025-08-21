package com.example.appcenter_project.repository.like;

import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.user.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TipLikeRepository extends JpaRepository<TipLike, Long> {
    Optional<TipLike> findByUserAndTip(User user, Tip tip);
    Boolean existsByUserAndTip(User user, Tip tip);
    boolean existsByUserIdAndTipId(Long userId, Long tipId);

    List<TipLike> findByUserId(Long userId);

    @Query("SELECT tl FROM TipLike tl JOIN FETCH tl.tip t WHERE tl.user.id = :userId")
    List<TipLike> findByUserIdWithTip(@Param("userId") Long userId);
}
