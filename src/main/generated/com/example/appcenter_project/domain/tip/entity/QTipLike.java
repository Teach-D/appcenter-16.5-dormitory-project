package com.example.appcenter_project.domain.tip.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTipLike is a Querydsl query type for TipLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTipLike extends EntityPathBase<TipLike> {

    private static final long serialVersionUID = -220742283L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTipLike tipLike = new QTipLike("tipLike");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final QTip tip;

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QTipLike(String variable) {
        this(TipLike.class, forVariable(variable), INITS);
    }

    public QTipLike(Path<? extends TipLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTipLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTipLike(PathMetadata metadata, PathInits inits) {
        this(TipLike.class, metadata, inits);
    }

    public QTipLike(Class<? extends TipLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.tip = inits.isInitialized("tip") ? new QTip(forProperty("tip"), inits.get("tip")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

