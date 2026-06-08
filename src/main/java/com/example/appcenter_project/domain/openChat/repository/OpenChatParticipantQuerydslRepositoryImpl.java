package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.QOpenChatParticipant;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenChatParticipantQuerydslRepositoryImpl implements OpenChatParticipantQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public OpenChatParticipantQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private static final QOpenChatParticipant openChatParticipant = QOpenChatParticipant.openChatParticipant;

    @Override
    public Map<Long, Long> countByRoomIds(List<Long> roomIds) {
        List<Tuple> results = queryFactory
                .select(openChatParticipant.roomId, openChatParticipant.count())
                .from(openChatParticipant)
                .where(openChatParticipant.roomId.in(roomIds))
                .groupBy(openChatParticipant.roomId)
                .fetch();

        Map<Long, Long> countMap = new HashMap<>();
        for (Tuple tuple : results) {
            Long roomId = tuple.get(openChatParticipant.roomId);
            Long count = tuple.get(openChatParticipant.count());
            if (roomId != null && count != null) {
                countMap.put(roomId, count);
            }
        }
        return countMap;
    }

    @Override
    public Set<Long> findJoinedRoomIds(Long userId, List<Long> roomIds) {
        List<Long> results = queryFactory
                .select(openChatParticipant.roomId)
                .from(openChatParticipant)
                .where(
                        openChatParticipant.roomId.in(roomIds),
                        openChatParticipant.userId.eq(userId)
                )
                .fetch();

        return new HashSet<>(results);
    }
}
