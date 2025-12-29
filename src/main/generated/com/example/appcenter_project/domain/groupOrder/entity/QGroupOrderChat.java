package com.example.appcenter_project.domain.groupOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupOrderChat is a Querydsl query type for GroupOrderChat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupOrderChat extends EntityPathBase<GroupOrderChat> {

    private static final long serialVersionUID = -606263858L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupOrderChat groupOrderChat = new QGroupOrderChat("groupOrderChat");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final QGroupOrderChatRoom groupOrderChatRoom;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final ListPath<Long, NumberPath<Long>> unreadUser = this.<Long, NumberPath<Long>>createList("unreadUser", Long.class, NumberPath.class, PathInits.DIRECT2);

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QGroupOrderChat(String variable) {
        this(GroupOrderChat.class, forVariable(variable), INITS);
    }

    public QGroupOrderChat(Path<? extends GroupOrderChat> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupOrderChat(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupOrderChat(PathMetadata metadata, PathInits inits) {
        this(GroupOrderChat.class, metadata, inits);
    }

    public QGroupOrderChat(Class<? extends GroupOrderChat> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.groupOrderChatRoom = inits.isInitialized("groupOrderChatRoom") ? new QGroupOrderChatRoom(forProperty("groupOrderChatRoom"), inits.get("groupOrderChatRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

