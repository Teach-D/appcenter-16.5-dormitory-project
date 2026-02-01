package com.example.appcenter_project.domain.roommate.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoommateChattingChat is a Querydsl query type for RoommateChattingChat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoommateChattingChat extends EntityPathBase<RoommateChattingChat> {

    private static final long serialVersionUID = -132510890L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoommateChattingChat roommateChattingChat = new QRoommateChattingChat("roommateChattingChat");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.appcenter_project.domain.user.entity.QUser member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final BooleanPath readByReceiver = createBoolean("readByReceiver");

    public final QRoommateChattingRoom roommateChattingRoom;

    public QRoommateChattingChat(String variable) {
        this(RoommateChattingChat.class, forVariable(variable), INITS);
    }

    public QRoommateChattingChat(Path<? extends RoommateChattingChat> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoommateChattingChat(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoommateChattingChat(PathMetadata metadata, PathInits inits) {
        this(RoommateChattingChat.class, metadata, inits);
    }

    public QRoommateChattingChat(Class<? extends RoommateChattingChat> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("member"), inits.get("member")) : null;
        this.roommateChattingRoom = inits.isInitialized("roommateChattingRoom") ? new QRoommateChattingRoom(forProperty("roommateChattingRoom"), inits.get("roommateChattingRoom")) : null;
    }

}

