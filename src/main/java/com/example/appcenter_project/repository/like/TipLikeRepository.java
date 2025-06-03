package com.example.appcenter_project.repository.like;

import com.example.appcenter_project.entity.like.TipLike;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipLikeRepository extends JpaRepository<TipLike, Long> {
    Optional<TipLike> findByUserAndTip(User user, Tip tip);

}
