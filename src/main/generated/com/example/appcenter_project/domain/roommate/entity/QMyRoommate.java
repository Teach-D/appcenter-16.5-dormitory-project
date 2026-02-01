package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMyRoommate is a Querydsl query type for MyRoommate
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMyRoommate extends EntityPathBase<MyRoommate> {

    private static final long serialVersionUID = 1863228484L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMyRoommate myRoommate = new QMyRoommate("myRoommate");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.appcenter_project.domain.user.entity.QUser roommate;

    public final ListPath<String, StringPath> rule = this.<String, StringPath>createList("rule", String.class, StringPath.class, PathInits.DIRECT2);

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QMyRoommate(String variable) {
        this(MyRoommate.class, forVariable(variable), INITS);
    }

    public QMyRoommate(Path<? extends MyRoommate> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMyRoommate(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMyRoommate(PathMetadata metadata, PathInits inits) {
        this(MyRoommate.class, metadata, inits);
    }

    public QMyRoommate(Class<? extends MyRoommate> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.roommate = inits.isInitialized("roommate") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("roommate"), inits.get("roommate")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

