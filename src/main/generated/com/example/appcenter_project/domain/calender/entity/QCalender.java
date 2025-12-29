package com.example.appcenter_project.domain.calender.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCalender is a Querydsl query type for Calender
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCalender extends EntityPathBase<Calender> {

    private static final long serialVersionUID = -1276413140L;

    public static final QCalender calender = new QCalender("calender");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath link = createString("link");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath title = createString("title");

    public QCalender(String variable) {
        super(Calender.class, forVariable(variable));
    }

    public QCalender(Path<? extends Calender> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCalender(PathMetadata metadata) {
        super(Calender.class, metadata);
    }

}

