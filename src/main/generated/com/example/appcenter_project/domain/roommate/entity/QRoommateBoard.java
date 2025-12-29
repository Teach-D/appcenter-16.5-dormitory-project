package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoommateBoard is a Querydsl query type for RoommateBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoommateBoard extends EntityPathBase<RoommateBoard> {

    private static final long serialVersionUID = -1787015858L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoommateBoard roommateBoard = new QRoommateBoard("roommateBoard");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isMatched = createBoolean("isMatched");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final NumberPath<Integer> roommateBoardLike = createNumber("roommateBoardLike", Integer.class);

    public final ListPath<RoommateBoardLike, QRoommateBoardLike> roommateBoardLikeList = this.<RoommateBoardLike, QRoommateBoardLike>createList("roommateBoardLikeList", RoommateBoardLike.class, QRoommateBoardLike.class, PathInits.DIRECT2);

    public final QRoommateCheckList roommateCheckList;

    public final StringPath title = createString("title");

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QRoommateBoard(String variable) {
        this(RoommateBoard.class, forVariable(variable), INITS);
    }

    public QRoommateBoard(Path<? extends RoommateBoard> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoommateBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoommateBoard(PathMetadata metadata, PathInits inits) {
        this(RoommateBoard.class, metadata, inits);
    }

    public QRoommateBoard(Class<? extends RoommateBoard> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.roommateCheckList = inits.isInitialized("roommateCheckList") ? new QRoommateCheckList(forProperty("roommateCheckList"), inits.get("roommateCheckList")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

