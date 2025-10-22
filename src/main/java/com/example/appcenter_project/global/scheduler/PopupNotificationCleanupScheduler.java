package com.example.appcenter_project.global.scheduler;

import com.example.appcenter_project.domain.notification.entity.PopupNotification;
import com.example.appcenter_project.domain.notification.repository.PopupNotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PopupNotificationCleanupScheduler {

    private final PopupNotificationRepository popupNotificationRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void deletePopupNotification() {
        LocalDate now = LocalDate.now();
        for (PopupNotification popupNotification : popupNotificationRepository.findAll()) {
            if(now.isAfter(popupNotification.getDeadline())) {
                popupNotificationRepository.delete(popupNotification);
            }
        }

    }
}
