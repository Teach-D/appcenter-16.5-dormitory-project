package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.QRoommateChattingChat;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingChat;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoommateChattingChatQuerydslRepositoryImpl implements RoommateChattingChatQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public RoommateChattingChatQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private static final QRoommateChattingChat chat = QRoommateChattingChat.roommateChattingChat;

    @Override
    public Map<Long, RoommateChattingChat> findLastMessagesByRoomIds(List<Long> roomIds) {
        if (roomIds.isEmpty()) return Map.of();

        JPQLQuery<Long> maxIdSubquery = JPAExpressions
                .select(chat.id.max())
                .from(chat)
                .where(chat.roommateChattingRoom.id.in(roomIds))
                .groupBy(chat.roommateChattingRoom.id);

        List<RoommateChattingChat> results = queryFactory
                .selectFrom(chat)
                .where(chat.id.in(maxIdSubquery))
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        c -> c.getRoommateChattingRoom().getId(),
                        c -> c));
    }

    @Override
    public Map<Long, Long> countUnreadByRoomIdsAndUserId(List<Long> roomIds, Long userId) {
        if (roomIds.isEmpty()) return Map.of();

        List<Tuple> results = queryFactory
                .select(chat.roommateChattingRoom.id, chat.count())
                .from(chat)
                .where(
                        chat.roommateChattingRoom.id.in(roomIds),
                        chat.member.id.ne(userId),
                        chat.readByReceiver.isFalse()
                )
                .groupBy(chat.roommateChattingRoom.id)
                .fetch();

        return results.stream()
                .collect(Collectors.toMap(
                        t -> t.get(chat.roommateChattingRoom.id),
                        t -> t.get(chat.count())));
    }
}
