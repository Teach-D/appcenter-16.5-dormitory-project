package com.example.appcenter_project.domain.feature.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFeature is a Querydsl query type for Feature
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeature extends EntityPathBase<Feature> {

    private static final long serialVersionUID = -245729186L;

    public static final QFeature feature = new QFeature("feature");

    public final BooleanPath flag = createBoolean("flag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath key = createString("key");

    public QFeature(String variable) {
        super(Feature.class, forVariable(variable));
    }

    public QFeature(Path<? extends Feature> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFeature(PathMetadata metadata) {
        super(Feature.class, metadata);
    }

}

