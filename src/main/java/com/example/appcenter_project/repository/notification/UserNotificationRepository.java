package com.example.appcenter_project.repository.notification;

import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.enums.user.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Optional<UserNotification> findByUserIdAndNotificationId(Long userId, Long notificationId);
    
    @Query("SELECT CASE WHEN COUNT(un) > 0 THEN true ELSE false END " +
           "FROM UserNotification un " +
           "WHERE un.user.id = :userId " +
           "AND un.notification.notificationType = :notificationType")
    boolean existsByUserIdAndNotificationType(@Param("userId") Long userId, 
                                               @Param("notificationType") NotificationType notificationType);
}
