package com.example.appcenter_project.domain.announcement.repository;

import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.appcenter_project.domain.announcement.entity.QAnnouncement.announcement;

@Repository
public class AnnouncementRepositoryImpl implements AnnouncementQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public AnnouncementRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Announcement> findAnnouncementComplex(AnnouncementType type, AnnouncementCategory category, String search) {
        return queryFactory
                .select(announcement)
                .from(announcement)
                .where(
                        announcementEqType(type),
                        announcementEqCategory(category),
                        announcementLikeSearch(search)
                ).fetch();
    }

    @Override
    public List<Announcement> findAllWithFilters(
            AnnouncementType type,
            AnnouncementCategory category,
            String search,
            Long lastId,
            Pageable pageable) {

        return queryFactory
                .selectFrom(announcement)
                .where(
                        announcementEqType(type),
                        announcementEqCategory(category),
                        announcementLikeSearch(search),
                        announcementIdLessThan(lastId)
                )
                .orderBy(announcement.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression announcementIdLessThan(Long lastId) {
        return lastId != null ? announcement.id.lt(lastId) : null;
    }

    private BooleanExpression announcementEqType(AnnouncementType type) {
        if (type == null || type == AnnouncementType.ALL) return null;
        return announcement.announcementType.eq(type);
    }

    private BooleanExpression announcementEqCategory(AnnouncementCategory category) {
        if (category == null || category == AnnouncementCategory.ALL) return null;
        return announcement.announcementCategory.eq(category);

    }

    private BooleanExpression announcementLikeSearch(String search) {
        if (search == null || search.isEmpty() || search.equals(" ")) {
            return null;
        } else {
            String trimmedSearch = search.trim();

            return announcement.title.contains(trimmedSearch)
                    .or(announcement.content.contains(trimmedSearch));
        }
    }

}
