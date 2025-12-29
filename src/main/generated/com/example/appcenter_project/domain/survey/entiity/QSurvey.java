package com.example.appcenter_project.domain.survey.entiity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurvey is a Querydsl query type for Survey
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurvey extends EntityPathBase<Survey> {

    private static final long serialVersionUID = 1982568241L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSurvey survey = new QSurvey("survey");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final com.example.appcenter_project.domain.user.entity.QUser creator;

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final ListPath<SurveyQuestion, QSurveyQuestion> questions = this.<SurveyQuestion, QSurveyQuestion>createList("questions", SurveyQuestion.class, QSurveyQuestion.class, PathInits.DIRECT2);

    public final NumberPath<Integer> recruitmentCount = createNumber("recruitmentCount", Integer.class);

    public final ListPath<SurveyResponse, QSurveyResponse> responses = this.<SurveyResponse, QSurveyResponse>createList("responses", SurveyResponse.class, QSurveyResponse.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final EnumPath<com.example.appcenter_project.domain.survey.enums.SurveyStatus> status = createEnum("status", com.example.appcenter_project.domain.survey.enums.SurveyStatus.class);

    public final StringPath title = createString("title");

    public QSurvey(String variable) {
        this(Survey.class, forVariable(variable), INITS);
    }

    public QSurvey(Path<? extends Survey> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSurvey(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSurvey(PathMetadata metadata, PathInits inits) {
        this(Survey.class, metadata, inits);
    }

    public QSurvey(Class<? extends Survey> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.creator = inits.isInitialized("creator") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("creator"), inits.get("creator")) : null;
    }

}

