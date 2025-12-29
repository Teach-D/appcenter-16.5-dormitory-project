package com.example.appcenter_project.domain.announcement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCrawledAnnouncement is a Querydsl query type for CrawledAnnouncement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrawledAnnouncement extends EntityPathBase<CrawledAnnouncement> {

    private static final long serialVersionUID = 1540621454L;

    public static final QCrawledAnnouncement crawledAnnouncement = new QCrawledAnnouncement("crawledAnnouncement");

    public final QAnnouncement _super = new QAnnouncement(this);

    //inherited
    public final EnumPath<com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory> announcementCategory = _super.announcementCategory;

    //inherited
    public final EnumPath<com.example.appcenter_project.domain.announcement.enums.AnnouncementType> announcementType = _super.announcementType;

    //inherited
    public final StringPath content = _super.content;

    public final ListPath<com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile, com.example.appcenter_project.common.file.entity.QCrawledAnnouncementFile> crawledAnnouncementFiles = this.<com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile, com.example.appcenter_project.common.file.entity.QCrawledAnnouncementFile>createList("crawledAnnouncementFiles", com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile.class, com.example.appcenter_project.common.file.entity.QCrawledAnnouncementFile.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> crawledDate = createDate("crawledDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath link = createString("link");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    public final StringPath number = createString("number");

    //inherited
    public final StringPath title = _super.title;

    //inherited
    public final NumberPath<Integer> viewCount = _super.viewCount;

    //inherited
    public final StringPath writer = _super.writer;

    public QCrawledAnnouncement(String variable) {
        super(CrawledAnnouncement.class, forVariable(variable));
    }

    public QCrawledAnnouncement(Path<? extends CrawledAnnouncement> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCrawledAnnouncement(PathMetadata metadata) {
        super(CrawledAnnouncement.class, metadata);
    }

}

