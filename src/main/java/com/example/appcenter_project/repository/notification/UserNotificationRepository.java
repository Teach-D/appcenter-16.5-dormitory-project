package com.example.appcenter_project.repository.notification;

import com.example.appcenter_project.entity.notification.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Optional<UserNotification> findByUserIdAndNotificationId(Long userId, Long notificationId);
}
