package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.entity.QOpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.QOpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class OpenChatRoomQuerydslRepositoryImpl implements OpenChatRoomQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public OpenChatRoomQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private static final QOpenChatRoom openChatRoom = QOpenChatRoom.openChatRoom;
    private static final QOpenChatParticipant openChatParticipant = QOpenChatParticipant.openChatParticipant;

    @Override
    public List<OpenChatRoom> findMyRooms(Long userId) {
        return queryFactory
                .selectFrom(openChatRoom)
                .join(openChatParticipant).on(
                        openChatRoom.id.eq(openChatParticipant.roomId),
                        openChatParticipant.userId.eq(userId)
                )
                .orderBy(
                        openChatRoom.lastMessageAt.desc().nullsLast(),
                        openChatRoom.createdDate.desc()
                )
                .fetch();
    }

    @Override
    public List<OpenChatRoom> findByDormitory(String dormType) {
        return queryFactory
                .selectFrom(openChatRoom)
                .where(
                        scopeEq(OpenChatRoomScope.DORMITORY),
                        creatorDormitoryEq(dormType)
                )
                .fetch();
    }

    @Override
    public List<OpenChatRoom> findAllPublicRooms() {
        return queryFactory
                .selectFrom(openChatRoom)
                .where(
                        openChatRoom.roomType.eq(OpenChatRoomType.OPEN)
                                .and(openChatRoom.scope.eq(OpenChatRoomScope.ALL))
                        .or(
                                openChatRoom.roomType.eq(OpenChatRoomType.DERIVED)
                                        .and(openChatRoom.isPublic.isTrue())
                        )
                )
                .fetch();
    }

    private BooleanExpression scopeEq(OpenChatRoomScope scope) {
        return scope != null ? openChatRoom.scope.eq(scope) : null;
    }

    private BooleanExpression creatorDormitoryEq(String dormType) {
        return dormType != null ? openChatRoom.creatorDormitory.eq(dormType) : null;
    }
}
