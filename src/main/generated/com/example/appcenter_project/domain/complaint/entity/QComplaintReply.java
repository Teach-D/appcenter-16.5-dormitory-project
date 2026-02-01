package com.example.appcenter_project.domain.complaint.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComplaintReply is a Querydsl query type for ComplaintReply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComplaintReply extends EntityPathBase<ComplaintReply> {

    private static final long serialVersionUID = 1395057996L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComplaintReply complaintReply = new QComplaintReply("complaintReply");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath attachmentUrl = createString("attachmentUrl");

    public final QComplaint complaint;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath replyContent = createString("replyContent");

    public final StringPath replyTitle = createString("replyTitle");

    public final com.example.appcenter_project.domain.user.entity.QUser responder;

    public final StringPath responderName = createString("responderName");

    public QComplaintReply(String variable) {
        this(ComplaintReply.class, forVariable(variable), INITS);
    }

    public QComplaintReply(Path<? extends ComplaintReply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComplaintReply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComplaintReply(PathMetadata metadata, PathInits inits) {
        this(ComplaintReply.class, metadata, inits);
    }

    public QComplaintReply(Class<? extends ComplaintReply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.complaint = inits.isInitialized("complaint") ? new QComplaint(forProperty("complaint"), inits.get("complaint")) : null;
        this.responder = inits.isInitialized("responder") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("responder"), inits.get("responder")) : null;
    }

}

