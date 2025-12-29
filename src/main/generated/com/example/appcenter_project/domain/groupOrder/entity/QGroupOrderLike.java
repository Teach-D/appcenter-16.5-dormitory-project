package com.example.appcenter_project.domain.groupOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGroupOrderLike is a Querydsl query type for GroupOrderLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupOrderLike extends EntityPathBase<GroupOrderLike> {

    private static final long serialVersionUID = -605994483L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGroupOrderLike groupOrderLike = new QGroupOrderLike("groupOrderLike");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final QGroupOrder groupOrder;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QGroupOrderLike(String variable) {
        this(GroupOrderLike.class, forVariable(variable), INITS);
    }

    public QGroupOrderLike(Path<? extends GroupOrderLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGroupOrderLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGroupOrderLike(PathMetadata metadata, PathInits inits) {
        this(GroupOrderLike.class, metadata, inits);
    }

    public QGroupOrderLike(Class<? extends GroupOrderLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.groupOrder = inits.isInitialized("groupOrder") ? new QGroupOrder(forProperty("groupOrder"), inits.get("groupOrder")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

