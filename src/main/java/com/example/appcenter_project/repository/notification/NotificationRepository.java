package com.example.appcenter_project.repository.notification;

import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.enums.user.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByBoardIdAndTitle(Long boardId, String title);
    Notification findByBoardIdAndTitle(Long boardId, String title);

    Notification findByBoardIdAndNotificationType(Long boardId, NotificationType notificationType);
}
