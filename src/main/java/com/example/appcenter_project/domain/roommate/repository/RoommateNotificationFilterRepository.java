package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateNotificationFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoommateNotificationFilterRepository extends JpaRepository<RoommateNotificationFilter, Long> {
    Optional<RoommateNotificationFilter> findByUserId(Long userId);

    @Query("SELECT f FROM RoommateNotificationFilter f " +
           "JOIN FETCH f.user u " +
           "LEFT JOIN FETCH u.receiveNotificationTypes")
    List<RoommateNotificationFilter> findAllWithUser();
}

