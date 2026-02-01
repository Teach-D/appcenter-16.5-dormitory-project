package com.example.appcenter_project.common.file.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrawledAnnouncementFile is a Querydsl query type for CrawledAnnouncementFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrawledAnnouncementFile extends EntityPathBase<CrawledAnnouncementFile> {

    private static final long serialVersionUID = -2132137220L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCrawledAnnouncementFile crawledAnnouncementFile = new QCrawledAnnouncementFile("crawledAnnouncementFile");

    public final QAttachedFile _super;

    // inherited
    public final com.example.appcenter_project.domain.announcement.entity.QAnnouncement announcement;

    // inherited
    public final com.example.appcenter_project.domain.complaint.entity.QComplaintReply complaintReply;

    public final com.example.appcenter_project.domain.announcement.entity.QCrawledAnnouncement crawledAnnouncement;

    //inherited
    public final StringPath fileName;

    //inherited
    public final StringPath filePath;

    //inherited
    public final NumberPath<Long> fileSize;

    //inherited
    public final NumberPath<Long> id;

    public QCrawledAnnouncementFile(String variable) {
        this(CrawledAnnouncementFile.class, forVariable(variable), INITS);
    }

    public QCrawledAnnouncementFile(Path<? extends CrawledAnnouncementFile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCrawledAnnouncementFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCrawledAnnouncementFile(PathMetadata metadata, PathInits inits) {
        this(CrawledAnnouncementFile.class, metadata, inits);
    }

    public QCrawledAnnouncementFile(Class<? extends CrawledAnnouncementFile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QAttachedFile(type, metadata, inits);
        this.announcement = _super.announcement;
        this.complaintReply = _super.complaintReply;
        this.crawledAnnouncement = inits.isInitialized("crawledAnnouncement") ? new com.example.appcenter_project.domain.announcement.entity.QCrawledAnnouncement(forProperty("crawledAnnouncement")) : null;
        this.fileName = _super.fileName;
        this.filePath = _super.filePath;
        this.fileSize = _super.fileSize;
        this.id = _super.id;
    }

}

