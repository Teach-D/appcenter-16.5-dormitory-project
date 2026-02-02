package com.example.appcenter_project.domain.notification.repository;

import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.appcenter_project.domain.notification.entity.QUserNotification.userNotification;
import static com.example.appcenter_project.domain.notification.entity.QNotification.notification;

@Repository
public class UserNotificationRepositoryImpl implements UserNotificationQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public UserNotificationRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<UserNotification> findAllWithFilters(
            Long userId,
            Long lastId,
            Pageable pageable) {

        return queryFactory
                .selectFrom(userNotification)
                .join(userNotification.notification, notification).fetchJoin()
                .where(
                        userNotificationEqUserId(userId),
                        userNotificationIdLessThan(lastId)
                )
                .orderBy(userNotification.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression userNotificationEqUserId(Long userId) {
        return userId != null ? userNotification.user.id.eq(userId) : null;
    }

    private BooleanExpression userNotificationIdLessThan(Long lastId) {
        return lastId != null ? userNotification.id.lt(lastId) : null;
    }
}