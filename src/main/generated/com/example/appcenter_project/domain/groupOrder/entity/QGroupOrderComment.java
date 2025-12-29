package com.example.appcenter_project.domain.groupOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupOrderComment is a Querydsl query type for GroupOrderComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupOrderComment extends EntityPathBase<GroupOrderComment> {

    private static final long serialVersionUID = -657735639L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupOrderComment groupOrderComment = new QGroupOrderComment("groupOrderComment");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final ListPath<GroupOrderComment, QGroupOrderComment> childGroupOrderComments = this.<GroupOrderComment, QGroupOrderComment>createList("childGroupOrderComments", GroupOrderComment.class, QGroupOrderComment.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final QGroupOrder groupOrder;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final QGroupOrderComment parentGroupOrderComment;

    public final StringPath reply = createString("reply");

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QGroupOrderComment(String variable) {
        this(GroupOrderComment.class, forVariable(variable), INITS);
    }

    public QGroupOrderComment(Path<? extends GroupOrderComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupOrderComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupOrderComment(PathMetadata metadata, PathInits inits) {
        this(GroupOrderComment.class, metadata, inits);
    }

    public QGroupOrderComment(Class<? extends GroupOrderComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.groupOrder = inits.isInitialized("groupOrder") ? new QGroupOrder(forProperty("groupOrder"), inits.get("groupOrder")) : null;
        this.parentGroupOrderComment = inits.isInitialized("parentGroupOrderComment") ? new QGroupOrderComment(forProperty("parentGroupOrderComment"), inits.get("parentGroupOrderComment")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

