package com.example.appcenter_project.domain.notification.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPopupNotification is a Querydsl query type for PopupNotification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPopupNotification extends EntityPathBase<PopupNotification> {

    private static final long serialVersionUID = -1369661964L;

    public static final QPopupNotification popupNotification = new QPopupNotification("popupNotification");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final DatePath<java.time.LocalDate> deadline = createDate("deadline", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final EnumPath<com.example.appcenter_project.domain.user.enums.NotificationType> notificationType = createEnum("notificationType", com.example.appcenter_project.domain.user.enums.NotificationType.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath title = createString("title");

    public QPopupNotification(String variable) {
        super(PopupNotification.class, forVariable(variable));
    }

    public QPopupNotification(Path<? extends PopupNotification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPopupNotification(PathMetadata metadata) {
        super(PopupNotification.class, metadata);
    }

}

