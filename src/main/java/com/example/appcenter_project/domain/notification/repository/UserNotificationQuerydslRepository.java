package com.example.appcenter_project.domain.notification.repository;

import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.shared.enums.ApiType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserNotificationQuerydslRepository {
    List<UserNotification> findAllWithFilters(Long userId, Long lastId, Pageable pageable);
    List<UserNotification> findByUserIdAndBoardIdAndApiType(Long userId, Long boardId, ApiType apiType);
}
