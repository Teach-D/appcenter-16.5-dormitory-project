package com.example.appcenter_project.domain.notification.repository;

import com.example.appcenter_project.domain.notification.entity.PopupNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PopupNotificationRepository extends JpaRepository<PopupNotification, Long> {

    @Query("SELECT p FROM PopupNotification p " +
            "WHERE p.startDate <= :now AND p.deadline >= :now")
    List<PopupNotification> findActivePopupNotifications(@Param("now") LocalDate now);
}
