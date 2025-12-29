package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoommateBoardLike is a Querydsl query type for RoommateBoardLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoommateBoardLike extends EntityPathBase<RoommateBoardLike> {

    private static final long serialVersionUID = -1191372283L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoommateBoardLike roommateBoardLike = new QRoommateBoardLike("roommateBoardLike");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final QRoommateBoard roommateBoard;

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QRoommateBoardLike(String variable) {
        this(RoommateBoardLike.class, forVariable(variable), INITS);
    }

    public QRoommateBoardLike(Path<? extends RoommateBoardLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoommateBoardLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoommateBoardLike(PathMetadata metadata, PathInits inits) {
        this(RoommateBoardLike.class, metadata, inits);
    }

    public QRoommateBoardLike(Class<? extends RoommateBoardLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.roommateBoard = inits.isInitialized("roommateBoard") ? new QRoommateBoard(forProperty("roommateBoard"), inits.get("roommateBoard")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

