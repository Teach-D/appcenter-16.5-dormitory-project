package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.dto.response.GroupOrderListProjection;
import com.example.appcenter_project.domain.groupOrder.dto.response.QGroupOrderListProjection;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderSort;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.domain.groupOrder.entity.QGroupOrder.groupOrder;


@Repository
public class GroupOrderRepositoryImpl implements GroupOrderQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public GroupOrderRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<GroupOrderListProjection> findGroupOrdersComplex(GroupOrderSort sort, GroupOrderType type, String search, Pageable pageable) {
        return queryFactory
                .select(new QGroupOrderListProjection(
                        groupOrder.id,
                        groupOrder.title,
                        groupOrder.groupOrderType,
                        groupOrder.price,
                        groupOrder.deadline,
                        groupOrder.recruitmentComplete,
                        groupOrder.groupOrderViewCount,
                        groupOrder.createdDate))
                .from(groupOrder)
                .where(
                        groupOrderEqType(type),
                        groupOrderLikeSearch(search)
                )
                .orderBy(groupOrderObSort(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    @Transactional
    public void bulkMarkExpired(List<Long> ids) {
        queryFactory
                .update(groupOrder)
                .set(groupOrder.recruitmentComplete, true)
                .where(groupOrder.id.in(ids))
                .execute();
    }

    private OrderSpecifier[] groupOrderObSort(GroupOrderSort sort) {
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        if (sort == GroupOrderSort.DEADLINE) {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("현재 시각: " + now); // 디버깅용

            NumberExpression<Integer> deadlineOrder = new CaseBuilder()
                    .when(groupOrder.deadline.after(now))
                    .then(1)
                    .otherwise(0);

            orderSpecifiers.add(new OrderSpecifier(Order.DESC, deadlineOrder)); // 마감 안된 것 먼저
            orderSpecifiers.add(new OrderSpecifier(Order.ASC, groupOrder.deadline)); // 그 다음 마감일 순

        } else if (sort == GroupOrderSort.POPULARITY) {
            orderSpecifiers.add(new OrderSpecifier(Order.DESC, groupOrder.groupOrderViewCount));

        } else if (sort == GroupOrderSort.PRICE) {
            orderSpecifiers.add(new OrderSpecifier(Order.ASC, groupOrder.price));
        } else if (sort == GroupOrderSort.LATEST) {
            orderSpecifiers.add(new OrderSpecifier(Order.DESC, groupOrder.id));
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }

    private BooleanExpression groupOrderLikeSearch(String search) {
        if (search == null || search.isEmpty() || search.equals(" ")) {
            return null;
        } else {
            String trimmedSearch = search.trim();

            return groupOrder.title.contains(trimmedSearch)
                    .or(groupOrder.description.contains(trimmedSearch));
        }
    }

    private BooleanExpression groupOrderEqType(GroupOrderType type) {
        if (type == null || type.equals(GroupOrderType.ALL)) {
            return null;
        } else {
            return groupOrder.groupOrderType.eq(type);
        }
    }

    @Override
    public List<GroupOrder> findUnnormalizedFoodOrders(int pageSize, Long lastId) {
        return queryFactory
                .selectFrom(groupOrder)
                .where(
                        groupOrder.groupOrderType.eq(GroupOrderType.FOOD),
                        groupOrder.place.isNull(),
                        lastId != null && lastId > 0 ? groupOrder.id.gt(lastId) : null
                )
                .orderBy(groupOrder.id.asc())
                .limit(pageSize)
                .fetch();
    }

    @Override
    public long countDistinctRawPlaceName() {
        Long result = queryFactory
                .select(groupOrder.rawPlaceName.lower().trim().countDistinct())
                .from(groupOrder)
                .where(groupOrder.rawPlaceName.isNotNull())
                .fetchOne();
        return result == null ? 0L : result;
    }

    @Override
    public long countDistinctPlaceId() {
        Long result = queryFactory
                .select(groupOrder.place.placeId.countDistinct())
                .from(groupOrder)
                .where(groupOrder.place.isNotNull())
                .fetchOne();
        return result == null ? 0L : result;
    }

    @Override
    public long countByCreatedAtAfter(LocalDateTime since) {
        Long result = queryFactory
                .select(groupOrder.count())
                .from(groupOrder)
                .where(
                        groupOrder.groupOrderType.eq(GroupOrderType.FOOD),
                        groupOrder.createdDate.after(since)
                )
                .fetchOne();
        return result == null ? 0L : result;
    }

    @Override
    public long countByCreatedAtAfterAndPlaceIsNull(LocalDateTime since) {
        Long result = queryFactory
                .select(groupOrder.count())
                .from(groupOrder)
                .where(
                        groupOrder.groupOrderType.eq(GroupOrderType.FOOD),
                        groupOrder.place.isNull(),
                        groupOrder.createdDate.after(since)
                )
                .fetchOne();
        return result == null ? 0L : result;
    }
}