package com.example.appcenter_project.domain.groupOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserGroupOrderChatRoom is a Querydsl query type for UserGroupOrderChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserGroupOrderChatRoom extends EntityPathBase<UserGroupOrderChatRoom> {

    private static final long serialVersionUID = -468356236L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserGroupOrderChatRoom userGroupOrderChatRoom = new QUserGroupOrderChatRoom("userGroupOrderChatRoom");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath chatRoomTitle = createString("chatRoomTitle");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final QGroupOrderChatRoom groupOrderChatRoom;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath recentChatContent = createString("recentChatContent");

    public final NumberPath<Integer> unreadCount = createNumber("unreadCount", Integer.class);

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QUserGroupOrderChatRoom(String variable) {
        this(UserGroupOrderChatRoom.class, forVariable(variable), INITS);
    }

    public QUserGroupOrderChatRoom(Path<? extends UserGroupOrderChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserGroupOrderChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserGroupOrderChatRoom(PathMetadata metadata, PathInits inits) {
        this(UserGroupOrderChatRoom.class, metadata, inits);
    }

    public QUserGroupOrderChatRoom(Class<? extends UserGroupOrderChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.groupOrderChatRoom = inits.isInitialized("groupOrderChatRoom") ? new QGroupOrderChatRoom(forProperty("groupOrderChatRoom"), inits.get("groupOrderChatRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

