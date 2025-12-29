package com.example.appcenter_project.domain.groupOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupOrder is a Querydsl query type for GroupOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupOrder extends EntityPathBase<GroupOrder> {

    private static final long serialVersionUID = 1921366358L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupOrder groupOrder = new QGroupOrder("groupOrder");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final DateTimePath<java.time.LocalDateTime> deadline = createDateTime("deadline", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final QGroupOrderChatRoom groupOrderChatRoom;

    public final ListPath<GroupOrderComment, QGroupOrderComment> groupOrderCommentList = this.<GroupOrderComment, QGroupOrderComment>createList("groupOrderCommentList", GroupOrderComment.class, QGroupOrderComment.class, PathInits.DIRECT2);

    public final NumberPath<Integer> groupOrderLike = createNumber("groupOrderLike", Integer.class);

    public final ListPath<GroupOrderLike, QGroupOrderLike> groupOrderLikeList = this.<GroupOrderLike, QGroupOrderLike>createList("groupOrderLikeList", GroupOrderLike.class, QGroupOrderLike.class, PathInits.DIRECT2);

    public final EnumPath<com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType> groupOrderType = createEnum("groupOrderType", com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType.class);

    public final NumberPath<Integer> groupOrderViewCount = createNumber("groupOrderViewCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath link = createString("link");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath openChatLink = createString("openChatLink");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final BooleanPath recruitmentComplete = createBoolean("recruitmentComplete");

    public final StringPath title = createString("title");

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QGroupOrder(String variable) {
        this(GroupOrder.class, forVariable(variable), INITS);
    }

    public QGroupOrder(Path<? extends GroupOrder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupOrder(PathMetadata metadata, PathInits inits) {
        this(GroupOrder.class, metadata, inits);
    }

    public QGroupOrder(Class<? extends GroupOrder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.groupOrderChatRoom = inits.isInitialized("groupOrderChatRoom") ? new QGroupOrderChatRoom(forProperty("groupOrderChatRoom"), inits.get("groupOrderChatRoom")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

