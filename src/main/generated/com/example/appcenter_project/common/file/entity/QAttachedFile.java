package com.example.appcenter_project.common.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAttachedFile is a Querydsl query type for AttachedFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttachedFile extends EntityPathBase<AttachedFile> {

    private static final long serialVersionUID = -1511142739L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAttachedFile attachedFile = new QAttachedFile("attachedFile");

    public final com.example.appcenter_project.domain.announcement.entity.QAnnouncement announcement;

    public final com.example.appcenter_project.domain.complaint.entity.QComplaintReply complaintReply;

    public final StringPath fileName = createString("fileName");

    public final StringPath filePath = createString("filePath");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QAttachedFile(String variable) {
        this(AttachedFile.class, forVariable(variable), INITS);
    }

    public QAttachedFile(Path<? extends AttachedFile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAttachedFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAttachedFile(PathMetadata metadata, PathInits inits) {
        this(AttachedFile.class, metadata, inits);
    }

    public QAttachedFile(Class<? extends AttachedFile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.announcement = inits.isInitialized("announcement") ? new com.example.appcenter_project.domain.announcement.entity.QAnnouncement(forProperty("announcement")) : null;
        this.complaintReply = inits.isInitialized("complaintReply") ? new com.example.appcenter_project.domain.complaint.entity.QComplaintReply(forProperty("complaintReply"), inits.get("complaintReply")) : null;
    }

}

