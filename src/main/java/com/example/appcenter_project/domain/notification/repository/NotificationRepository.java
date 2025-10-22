package com.example.appcenter_project.domain.notification.repository;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByBoardIdAndTitle(Long boardId, String title);
    Notification findByBoardIdAndTitle(Long boardId, String title);

    Notification findByBoardIdAndNotificationType(Long boardId, NotificationType notificationType);
}
