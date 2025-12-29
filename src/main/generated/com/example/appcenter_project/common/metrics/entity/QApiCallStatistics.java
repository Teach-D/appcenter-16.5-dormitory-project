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

    public final StringPath apiName = createString("apiName");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastCallTime = createDateTime("lastCallTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> totalCalls = createNumber("totalCalls", Long.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

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

