package com.example.appcenter_project.domain.groupOrder.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGroupOrderPopularSearchKeyword is a Querydsl query type for GroupOrderPopularSearchKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupOrderPopularSearchKeyword extends EntityPathBase<GroupOrderPopularSearchKeyword> {

    private static final long serialVersionUID = 1203203902L;

    public static final QGroupOrderPopularSearchKeyword groupOrderPopularSearchKeyword = new QGroupOrderPopularSearchKeyword("groupOrderPopularSearchKeyword");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath keyword = createString("keyword");

    public final NumberPath<Integer> searchCount = createNumber("searchCount", Integer.class);

    public QGroupOrderPopularSearchKeyword(String variable) {
        super(GroupOrderPopularSearchKeyword.class, forVariable(variable));
    }

    public QGroupOrderPopularSearchKeyword(Path<? extends GroupOrderPopularSearchKeyword> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGroupOrderPopularSearchKeyword(PathMetadata metadata) {
        super(GroupOrderPopularSearchKeyword.class, metadata);
    }

}

