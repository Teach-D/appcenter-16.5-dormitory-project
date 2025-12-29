package com.example.appcenter_project.domain.survey.entiity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurveyOption is a Querydsl query type for SurveyOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurveyOption extends EntityPathBase<SurveyOption> {

    private static final long serialVersionUID = 1679762182L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSurveyOption surveyOption = new QSurveyOption("surveyOption");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> optionOrder = createNumber("optionOrder", Integer.class);

    public final StringPath optionText = createString("optionText");

    public final QSurveyQuestion question;

    public QSurveyOption(String variable) {
        this(SurveyOption.class, forVariable(variable), INITS);
    }

    public QSurveyOption(Path<? extends SurveyOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSurveyOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSurveyOption(PathMetadata metadata, PathInits inits) {
        this(SurveyOption.class, metadata, inits);
    }

    public QSurveyOption(Class<? extends SurveyOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.question = inits.isInitialized("question") ? new QSurveyQuestion(forProperty("question"), inits.get("question")) : null;
    }

}

