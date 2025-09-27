package com.example.appcenter_project.repository.notification;

import com.example.appcenter_project.entity.notification.PopupNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopupNotificationRepository extends JpaRepository<PopupNotification, Long> {
}
