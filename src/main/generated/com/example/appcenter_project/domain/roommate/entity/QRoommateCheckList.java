package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoommateCheckList is a Querydsl query type for RoommateCheckList
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoommateCheckList extends EntityPathBase<RoommateCheckList> {

    private static final long serialVersionUID = 813445166L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoommateCheckList roommateCheckList = new QRoommateCheckList("roommateCheckList");

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.CleanlinessType> arrangement = createEnum("arrangement", com.example.appcenter_project.domain.roommate.enums.CleanlinessType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.BedTimeType> bedTime = createEnum("bedTime", com.example.appcenter_project.domain.roommate.enums.BedTimeType.class);

    public final EnumPath<com.example.appcenter_project.domain.user.enums.College> college = createEnum("college", com.example.appcenter_project.domain.user.enums.College.class);

    public final StringPath comment = createString("comment");

    public final SetPath<com.example.appcenter_project.domain.roommate.enums.DormDay, EnumPath<com.example.appcenter_project.domain.roommate.enums.DormDay>> dormPeriod = this.<com.example.appcenter_project.domain.roommate.enums.DormDay, EnumPath<com.example.appcenter_project.domain.roommate.enums.DormDay>>createSet("dormPeriod", com.example.appcenter_project.domain.roommate.enums.DormDay.class, EnumPath.class, PathInits.DIRECT2);

    public final EnumPath<com.example.appcenter_project.domain.user.enums.DormType> dormType = createEnum("dormType", com.example.appcenter_project.domain.user.enums.DormType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath mbti = createString("mbti");

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.ReligionType> religion = createEnum("religion", com.example.appcenter_project.domain.roommate.enums.ReligionType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.ShowerTimeType> showerHour = createEnum("showerHour", com.example.appcenter_project.domain.roommate.enums.ShowerTimeType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.ShowerDurationType> showerTime = createEnum("showerTime", com.example.appcenter_project.domain.roommate.enums.ShowerDurationType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.SleepSensitivityType> sleeper = createEnum("sleeper", com.example.appcenter_project.domain.roommate.enums.SleepSensitivityType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.SmokingType> smoking = createEnum("smoking", com.example.appcenter_project.domain.roommate.enums.SmokingType.class);

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.SnoringType> snoring = createEnum("snoring", com.example.appcenter_project.domain.roommate.enums.SnoringType.class);

    public final StringPath title = createString("title");

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.TeethGrindingType> toothGrind = createEnum("toothGrind", com.example.appcenter_project.domain.roommate.enums.TeethGrindingType.class);

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QRoommateCheckList(String variable) {
        this(RoommateCheckList.class, forVariable(variable), INITS);
    }

    public QRoommateCheckList(Path<? extends RoommateCheckList> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoommateCheckList(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoommateCheckList(PathMetadata metadata, PathInits inits) {
        this(RoommateCheckList.class, metadata, inits);
    }

    public QRoommateCheckList(Class<? extends RoommateCheckList> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

