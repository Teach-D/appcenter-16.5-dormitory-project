package com.example.appcenter_project.domain.survey.entiity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurveyAnswer is a Querydsl query type for SurveyAnswer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyAnswer extends EntityPathBase<SurveyAnswer> {

    private static final long serialVersionUID = 1277090383L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSurveyAnswer surveyAnswer = new QSurveyAnswer("surveyAnswer");

    public final StringPath answerText = createString("answerText");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSurveyQuestion question;

    public final QSurveyResponse response;

    public final ListPath<SurveyOption, QSurveyOption> selectedOptions = this.<SurveyOption, QSurveyOption>createList("selectedOptions", SurveyOption.class, QSurveyOption.class, PathInits.DIRECT2);

    public QSurveyAnswer(String variable) {
        this(SurveyAnswer.class, forVariable(variable), INITS);
    }

    public QSurveyAnswer(Path<? extends SurveyAnswer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSurveyAnswer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSurveyAnswer(PathMetadata metadata, PathInits inits) {
        this(SurveyAnswer.class, metadata, inits);
    }

    public QSurveyAnswer(Class<? extends SurveyAnswer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.question = inits.isInitialized("question") ? new QSurveyQuestion(forProperty("question"), inits.get("question")) : null;
        this.response = inits.isInitialized("response") ? new QSurveyResponse(forProperty("response"), inits.get("response")) : null;
    }

}

