package com.example.appcenter_project.domain.announcement.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QManualAnnouncement is a Querydsl query type for ManualAnnouncement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QManualAnnouncement extends EntityPathBase<ManualAnnouncement> {

    private static final long serialVersionUID = 1671180012L;

    public static final QManualAnnouncement manualAnnouncement = new QManualAnnouncement("manualAnnouncement");

    public final QAnnouncement _super = new QAnnouncement(this);

    //inherited
    public final EnumPath<com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory> announcementCategory = _super.announcementCategory;

    //inherited
    public final EnumPath<com.example.appcenter_project.domain.announcement.enums.AnnouncementType> announcementType = _super.announcementType;

    //inherited
    public final StringPath content = _super.content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isEmergency = createBoolean("isEmergency");

    public final ListPath<com.example.appcenter_project.common.file.entity.ManualAnnouncementFile, com.example.appcenter_project.common.file.entity.QManualAnnouncementFile> manualAnnouncementFiles = this.<com.example.appcenter_project.common.file.entity.ManualAnnouncementFile, com.example.appcenter_project.common.file.entity.QManualAnnouncementFile>createList("manualAnnouncementFiles", com.example.appcenter_project.common.file.entity.ManualAnnouncementFile.class, com.example.appcenter_project.common.file.entity.QManualAnnouncementFile.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDate = _super.modifiedDate;

    //inherited
    public final StringPath title = _super.title;

    //inherited
    public final NumberPath<Integer> viewCount = _super.viewCount;

    //inherited
    public final StringPath writer = _super.writer;

    public QManualAnnouncement(String variable) {
        super(ManualAnnouncement.class, forVariable(variable));
    }

    public QManualAnnouncement(Path<? extends ManualAnnouncement> path) {
        super(path.getType(), path.getMetadata());
    }

    public QManualAnnouncement(PathMetadata metadata) {
        super(ManualAnnouncement.class, metadata);
    }

}

