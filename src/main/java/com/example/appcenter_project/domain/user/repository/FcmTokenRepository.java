package com.example.appcenter_project.domain.user.repository;

import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken,Long> {
    void deleteByToken(String targetToken);
    boolean existsByToken(String token);
    Optional<FcmToken> findByUser(User user);

    FcmToken findByToken(String token);
}
