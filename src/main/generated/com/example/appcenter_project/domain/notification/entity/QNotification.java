package com.example.appcenter_project.domain.notification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = -1797692274L;

    public static final QNotification notification = new QNotification("notification");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final EnumPath<com.example.appcenter_project.shared.enums.ApiType> apiType = createEnum("apiType", com.example.appcenter_project.shared.enums.ApiType.class);

    public final NumberPath<Long> boardId = createNumber("boardId", Long.class);

    public final StringPath body = createString("body");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final EnumPath<com.example.appcenter_project.domain.user.enums.NotificationType> notificationType = createEnum("notificationType", com.example.appcenter_project.domain.user.enums.NotificationType.class);

    public final StringPath title = createString("title");

    public final ListPath<UserNotification, QUserNotification> userNotifications = this.<UserNotification, QUserNotification>createList("userNotifications", UserNotification.class, QUserNotification.class, PathInits.DIRECT2);

    public QNotification(String variable) {
        super(Notification.class, forVariable(variable));
    }

    public QNotification(Path<? extends Notification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotification(PathMetadata metadata) {
        super(Notification.class, metadata);
    }

}

