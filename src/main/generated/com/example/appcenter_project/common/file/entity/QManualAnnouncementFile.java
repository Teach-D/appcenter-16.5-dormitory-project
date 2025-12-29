package com.example.appcenter_project.common.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QManualAnnouncementFile is a Querydsl query type for ManualAnnouncementFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QManualAnnouncementFile extends EntityPathBase<ManualAnnouncementFile> {

    private static final long serialVersionUID = -1314927498L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QManualAnnouncementFile manualAnnouncementFile = new QManualAnnouncementFile("manualAnnouncementFile");

    public final QAttachedFile _super;

    // inherited
    public final com.example.appcenter_project.domain.announcement.entity.QAnnouncement announcement;

    // inherited
    public final com.example.appcenter_project.domain.complaint.entity.QComplaintReply complaintReply;

    //inherited
    public final StringPath fileName;

    //inherited
    public final StringPath filePath;

    //inherited
    public final NumberPath<Long> fileSize;

    //inherited
    public final NumberPath<Long> id;

    public final com.example.appcenter_project.domain.announcement.entity.QManualAnnouncement manualAnnouncement;

    public QManualAnnouncementFile(String variable) {
        this(ManualAnnouncementFile.class, forVariable(variable), INITS);
    }

    public QManualAnnouncementFile(Path<? extends ManualAnnouncementFile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QManualAnnouncementFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QManualAnnouncementFile(PathMetadata metadata, PathInits inits) {
        this(ManualAnnouncementFile.class, metadata, inits);
    }

    public QManualAnnouncementFile(Class<? extends ManualAnnouncementFile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QAttachedFile(type, metadata, inits);
        this.announcement = _super.announcement;
        this.complaintReply = _super.complaintReply;
        this.fileName = _super.fileName;
        this.filePath = _super.filePath;
        this.fileSize = _super.fileSize;
        this.id = _super.id;
        this.manualAnnouncement = inits.isInitialized("manualAnnouncement") ? new com.example.appcenter_project.domain.announcement.entity.QManualAnnouncement(forProperty("manualAnnouncement")) : null;
    }

}

