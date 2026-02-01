package com.example.appcenter_project.common.metrics.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QApiCallStatistics is a Querydsl query type for ApiCallStatistics
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApiCallStatistics extends EntityPathBase<ApiCallStatistics> {

    private static final long serialVersionUID = -941282097L;

    public static final QApiCallStatistics apiCallStatistics = new QApiCallStatistics("apiCallStatistics");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath apiPath = createString("apiPath");

    public final NumberPath<Long> callCount = createNumber("callCount", Long.class);

    public final DatePath<java.time.LocalDate> callDate = createDate("callDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final StringPath httpMethod = createString("httpMethod");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastCalledAt = createDateTime("lastCalledAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public QApiCallStatistics(String variable) {
        super(ApiCallStatistics.class, forVariable(variable));
    }

    public QApiCallStatistics(Path<? extends ApiCallStatistics> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApiCallStatistics(PathMetadata metadata) {
        super(ApiCallStatistics.class, metadata);
    }

}

