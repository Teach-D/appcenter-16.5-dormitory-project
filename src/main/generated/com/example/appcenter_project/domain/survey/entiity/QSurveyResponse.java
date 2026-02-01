package com.example.appcenter_project.domain.survey.entiity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurveyResponse is a Querydsl query type for SurveyResponse
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyResponse extends EntityPathBase<SurveyResponse> {

    private static final long serialVersionUID = -917549422L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSurveyResponse surveyResponse = new QSurveyResponse("surveyResponse");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final ListPath<SurveyAnswer, QSurveyAnswer> answers = this.<SurveyAnswer, QSurveyAnswer>createList("answers", SurveyAnswer.class, QSurveyAnswer.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final QSurvey survey;

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QSurveyResponse(String variable) {
        this(SurveyResponse.class, forVariable(variable), INITS);
    }

    public QSurveyResponse(Path<? extends SurveyResponse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSurveyResponse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSurveyResponse(PathMetadata metadata, PathInits inits) {
        this(SurveyResponse.class, metadata, inits);
    }

    public QSurveyResponse(Class<? extends SurveyResponse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.survey = inits.isInitialized("survey") ? new QSurvey(forProperty("survey"), inits.get("survey")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

