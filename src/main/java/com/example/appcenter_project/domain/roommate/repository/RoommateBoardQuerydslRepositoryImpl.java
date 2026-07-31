package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.QRoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.QRoommateCheckList;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class RoommateBoardQuerydslRepositoryImpl implements RoommateBoardQuerydslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QRoommateBoard board = QRoommateBoard.roommateBoard;
    private static final QRoommateCheckList cl = QRoommateCheckList.roommateCheckList;

    public RoommateBoardQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<RoommateBoard> searchBoards(Long lastId, int size, String keyword, Integer year, SemesterType semester) {
        return queryFactory
                .selectFrom(board)
                .join(board.roommateCheckList, cl).fetchJoin()
                .where(
                        cursorId(lastId),
                        keywordContains(keyword),
                        yearEq(year),
                        semesterEq(semester)
                )
                .orderBy(board.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression cursorId(Long lastId) {
        return lastId != null ? board.id.lt(lastId) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return cl.title.containsIgnoreCase(keyword).or(cl.comment.containsIgnoreCase(keyword));
    }

    private BooleanExpression yearEq(Integer year) {
        return year != null ? board.year.eq(year) : null;
    }

    private BooleanExpression semesterEq(SemesterType semester) {
        return semester != null ? board.semester.eq(semester) : null;
    }
}
