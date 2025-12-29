package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoommateChattingRoom is a Querydsl query type for RoommateChattingRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoommateChattingRoom extends EntityPathBase<RoommateChattingRoom> {

    private static final long serialVersionUID = -132056871L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoommateChattingRoom roommateChattingRoom = new QRoommateChattingRoom("roommateChattingRoom");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final ListPath<RoommateChattingChat, QRoommateChattingChat> chattingChatList = this.<RoommateChattingChat, QRoommateChattingChat>createList("chattingChatList", RoommateChattingChat.class, QRoommateChattingChat.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final com.example.appcenter_project.domain.user.entity.QUser guest;

    public final QRoommateCheckList guestChecklist;

    public final com.example.appcenter_project.domain.user.entity.QUser host;

    public final QRoommateCheckList hostChecklist;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final QRoommateBoard roommateBoard;

    public QRoommateChattingRoom(String variable) {
        this(RoommateChattingRoom.class, forVariable(variable), INITS);
    }

    public QRoommateChattingRoom(Path<? extends RoommateChattingRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoommateChattingRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoommateChattingRoom(PathMetadata metadata, PathInits inits) {
        this(RoommateChattingRoom.class, metadata, inits);
    }

    public QRoommateChattingRoom(Class<? extends RoommateChattingRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.guest = inits.isInitialized("guest") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("guest"), inits.get("guest")) : null;
        this.guestChecklist = inits.isInitialized("guestChecklist") ? new QRoommateCheckList(forProperty("guestChecklist"), inits.get("guestChecklist")) : null;
        this.host = inits.isInitialized("host") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("host"), inits.get("host")) : null;
        this.hostChecklist = inits.isInitialized("hostChecklist") ? new QRoommateCheckList(forProperty("hostChecklist"), inits.get("hostChecklist")) : null;
        this.roommateBoard = inits.isInitialized("roommateBoard") ? new QRoommateBoard(forProperty("roommateBoard"), inits.get("roommateBoard")) : null;
    }

}

