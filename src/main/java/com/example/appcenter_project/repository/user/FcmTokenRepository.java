package com.example.appcenter_project.repository.user;

import com.example.appcenter_project.entity.user.FcmToken;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken,Long> {
    void deleteByToken(String targetToken);
    boolean existsByToken(String token);
    Optional<FcmToken> findByUser(User user);

    FcmToken findByToken(String token);
}
