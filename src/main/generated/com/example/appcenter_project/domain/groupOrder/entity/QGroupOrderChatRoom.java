package com.example.appcenter_project.domain.groupOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupOrderChatRoom is a Querydsl query type for GroupOrderChatRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupOrderChatRoom extends EntityPathBase<GroupOrderChatRoom> {

    private static final long serialVersionUID = -1170177079L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupOrderChatRoom groupOrderChatRoom = new QGroupOrderChatRoom("groupOrderChatRoom");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final QGroupOrder groupOrder;

    public final ListPath<GroupOrderChat, QGroupOrderChat> groupOrderChatList = this.<GroupOrderChat, QGroupOrderChat>createList("groupOrderChatList", GroupOrderChat.class, QGroupOrderChat.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath title = createString("title");

    public final ListPath<UserGroupOrderChatRoom, QUserGroupOrderChatRoom> userGroupOrderChatRoomList = this.<UserGroupOrderChatRoom, QUserGroupOrderChatRoom>createList("userGroupOrderChatRoomList", UserGroupOrderChatRoom.class, QUserGroupOrderChatRoom.class, PathInits.DIRECT2);

    public QGroupOrderChatRoom(String variable) {
        this(GroupOrderChatRoom.class, forVariable(variable), INITS);
    }

    public QGroupOrderChatRoom(Path<? extends GroupOrderChatRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupOrderChatRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupOrderChatRoom(PathMetadata metadata, PathInits inits) {
        this(GroupOrderChatRoom.class, metadata, inits);
    }

    public QGroupOrderChatRoom(Class<? extends GroupOrderChatRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.groupOrder = inits.isInitialized("groupOrder") ? new QGroupOrder(forProperty("groupOrder"), inits.get("groupOrder")) : null;
    }

}

