package com.example.appcenter_project.domain.notification.repository;

import com.example.appcenter_project.domain.notification.entity.PopupNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopupNotificationRepository extends JpaRepository<PopupNotification, Long> {
}
