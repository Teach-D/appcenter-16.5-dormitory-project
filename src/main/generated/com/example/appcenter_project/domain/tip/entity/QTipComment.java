package com.example.appcenter_project.domain.tip.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTipComment is a Querydsl query type for TipComment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTipComment extends EntityPathBase<TipComment> {

    private static final long serialVersionUID = 237939649L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTipComment tipComment = new QTipComment("tipComment");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final ListPath<TipComment, QTipComment> childTipComments = this.<TipComment, QTipComment>createList("childTipComments", TipComment.class, QTipComment.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final QTipComment parentTipComment;

    public final StringPath reply = createString("reply");

    public final QTip tip;

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QTipComment(String variable) {
        this(TipComment.class, forVariable(variable), INITS);
    }

    public QTipComment(Path<? extends TipComment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTipComment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTipComment(PathMetadata metadata, PathInits inits) {
        this(TipComment.class, metadata, inits);
    }

    public QTipComment(Class<? extends TipComment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parentTipComment = inits.isInitialized("parentTipComment") ? new QTipComment(forProperty("parentTipComment"), inits.get("parentTipComment")) : null;
        this.tip = inits.isInitialized("tip") ? new QTip(forProperty("tip"), inits.get("tip")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

