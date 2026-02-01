package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoommateMatching is a Querydsl query type for RoommateMatching
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoommateMatching extends EntityPathBase<RoommateMatching> {

    private static final long serialVersionUID = 2111427733L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoommateMatching roommateMatching = new QRoommateMatching("roommateMatching");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.appcenter_project.domain.user.entity.QUser receiver;

    public final com.example.appcenter_project.domain.user.entity.QUser sender;

    public final EnumPath<com.example.appcenter_project.domain.roommate.enums.MatchingStatus> status = createEnum("status", com.example.appcenter_project.domain.roommate.enums.MatchingStatus.class);

    public QRoommateMatching(String variable) {
        this(RoommateMatching.class, forVariable(variable), INITS);
    }

    public QRoommateMatching(Path<? extends RoommateMatching> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoommateMatching(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoommateMatching(PathMetadata metadata, PathInits inits) {
        this(RoommateMatching.class, metadata, inits);
    }

    public QRoommateMatching(Class<? extends RoommateMatching> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.receiver = inits.isInitialized("receiver") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("receiver"), inits.get("receiver")) : null;
        this.sender = inits.isInitialized("sender") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("sender"), inits.get("sender")) : null;
    }

}

