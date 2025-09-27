package com.example.appcenter_project.repository.notification;

import com.example.appcenter_project.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
