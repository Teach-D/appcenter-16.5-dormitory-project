package com.example.appcenter_project.repository.user;

import com.example.appcenter_project.entity.user.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken,Long> {
    void deleteByToken(String targetToken);
}
