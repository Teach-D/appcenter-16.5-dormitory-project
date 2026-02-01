package com.example.appcenter_project.domain.complaint.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComplaint is a Querydsl query type for Complaint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComplaint extends EntityPathBase<Complaint> {

    private static final long serialVersionUID = -889637826L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComplaint complaint = new QComplaint("complaint");

    public final com.example.appcenter_project.common.QBaseTimeEntity _super = new com.example.appcenter_project.common.QBaseTimeEntity(this);

    public final StringPath bedNumber = createString("bedNumber");

    public final EnumPath<com.example.appcenter_project.domain.complaint.enums.DormBuilding> building = createEnum("building", com.example.appcenter_project.domain.complaint.enums.DormBuilding.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final EnumPath<com.example.appcenter_project.domain.user.enums.DormType> dormType = createEnum("dormType", com.example.appcenter_project.domain.user.enums.DormType.class);

    public final StringPath floor = createString("floor");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath incidentDate = createString("incidentDate");

    public final StringPath incidentTime = createString("incidentTime");

    public final BooleanPath isPrivacyAgreed = createBoolean("isPrivacyAgreed");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath officer = createString("officer");

    public final QComplaintReply reply;

    public final StringPath roomNumber = createString("roomNumber");

    public final StringPath specificLocation = createString("specificLocation");

    public final EnumPath<com.example.appcenter_project.domain.complaint.enums.ComplaintStatus> status = createEnum("status", com.example.appcenter_project.domain.complaint.enums.ComplaintStatus.class);

    public final StringPath title = createString("title");

    public final EnumPath<com.example.appcenter_project.domain.complaint.enums.ComplaintType> type = createEnum("type", com.example.appcenter_project.domain.complaint.enums.ComplaintType.class);

    public final com.example.appcenter_project.domain.user.entity.QUser user;

    public QComplaint(String variable) {
        this(Complaint.class, forVariable(variable), INITS);
    }

    public QComplaint(Path<? extends Complaint> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComplaint(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComplaint(PathMetadata metadata, PathInits inits) {
        this(Complaint.class, metadata, inits);
    }

    public QComplaint(Class<? extends Complaint> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reply = inits.isInitialized("reply") ? new QComplaintReply(forProperty("reply"), inits.get("reply")) : null;
        this.user = inits.isInitialized("user") ? new com.example.appcenter_project.domain.user.entity.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

