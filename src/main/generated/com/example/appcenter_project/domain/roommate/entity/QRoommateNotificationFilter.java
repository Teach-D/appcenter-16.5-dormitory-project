package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoommateNotificationFilter is a Querydsl query type for RoommateNotificationFilter
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoommateNotificationFilter extends EntityPathBase<RoommateNotificationFilter> {

    private static final long serialVersionUID = 1197076187L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoommateNotificationFilter roommateNotificationFilter = new QRoommateNotificationFilter("roommateNotificationFilter");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.CleanlinessType> arrangement = createEnum("arrangement", com.example.appcenter_project.domain.roommate.enums.CleanlinessType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.BedTimeType> bedTime = createEnum("bedTime", com.example.appcenter_project.domain.roommate.enums.BedTimeType.class);

    public final SetPath<com.example.appcenter_project.domain.user.enums.College, EnumPath<com.example.appcenter_project.domain.user.enums.College>> colleges = this.<com.example.appcenter_project.domain.user.enums.College, EnumPath<com.example.appcenter_project.domain.user.enums.College>>createSet("colleges", com.example.appcenter_project.domain.user.enums.College.class, EnumPath.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final SetPath<com.example.appcenter_project.domain.roommate.enums.DormDay, EnumPath<com.example.appcenter_project.domain.roommate.enums.DormDay>> dormPeriodDays = this.<com.example.appcenter_project.domain.roommate.enums.DormDay, EnumPath<com.example.appcenter_project.domain.roommate.enums.DormDay>>createSet("dormPeriodDays", com.example.appcenter_project.domain.roommate.enums.DormDay.class, EnumPath.class, PathInits.DIRECT2);

    public final EnumPath<com.example.appcenter_project.domain.user.enums.DormType> dormType = createEnum("dormType", com.example.appcenter_project.domain.user.enums.DormType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final SetPath<com.example.appcenter_project.domain.roommate.enums.ReligionType, EnumPath<com.example.appcenter_project.domain.roommate.enums.ReligionType>> religions = this.<com.example.appcenter_project.domain.roommate.enums.ReligionType, EnumPath<com.example.appcenter_project.domain.roommate.enums.ReligionType>>createSet("religions", com.example.appcenter_project.domain.roommate.enums.ReligionType.class, EnumPath.class, PathInits.DIRECT2);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.ShowerTimeType> showerHour = createEnum("showerHour", com.example.appcenter_project.domain.roommate.enums.ShowerTimeType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.ShowerDurationType> showerTime = createEnum("showerTime", com.example.appcenter_project.domain.roommate.enums.ShowerDurationType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.SleepSensitivityType> sleeper = createEnum("sleeper", com.example.appcenter_project.domain.roommate.enums.SleepSensitivityType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.SmokingType> smoking = createEnum("smoking", com.example.appcenter_project.domain.roommate.enums.SmokingType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.SnoringType> snoring = createEnum("snoring", com.example.appcenter_project.domain.roommate.enums.SnoringType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.TeethGrindingType> toothGrind = createEnum("toothGrind", com.example.appcenter_project.domain.roommate.enums.TeethGrindingType.class);

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QRoommateNotificationFilter(String variable) {
        this(RoommateNotificationFilter.class, forVariable(variable), INITS);
    }

    public QRoommateNotificationFilter(Path<? extends RoommateNotificationFilter> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoommateNotificationFilter(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoommateNotificationFilter(PathMetadata metadata, PathInits inits) {
        this(RoommateNotificationFilter.class, metadata, inits);
    }

    public QRoommateNotificationFilter(Class<? extends RoommateNotificationFilter> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

