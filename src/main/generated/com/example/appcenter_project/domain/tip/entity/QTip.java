package com.example.appcenter_project.domain.tip.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTip is a Querydsl query type for Tip
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTip extends EntityPathBase<Tip> {

    private static final long serialVersionUID = -846059330L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTip tip = new QTip("tip");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final NumberPath<Integer> tipCommentCount = createNumber("tipCommentCount", Integer.class);

    public final ListPath<TipComment, QTipComment> tipCommentList = this.<TipComment, QTipComment>createList("tipCommentList", TipComment.class, QTipComment.class, PathInits.DIRECT2);

    public final NumberPath<Integer> tipLike = createNumber("tipLike", Integer.class);

    public final ListPath<TipLike, QTipLike> tipLikeList = this.<TipLike, QTipLike>createList("tipLikeList", TipLike.class, QTipLike.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QTip(String variable) {
        this(Tip.class, forVariable(variable), INITS);
    }

    public QTip(Path<? extends Tip> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTip(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTip(PathMetadata metadata, PathInits inits) {
        this(Tip.class, metadata, inits);
    }

    public QTip(Class<? extends Tip> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

