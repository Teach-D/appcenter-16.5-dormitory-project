package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.QGroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.entity.groupOrder.QGroupOrder.*;

@Repository
public class GroupOrderRepositoryImpl implements GroupOrderQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public GroupOrderRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<GroupOrder> findGroupOrdersComplex(GroupOrderSort sort, GroupOrderType type, String search) {
        return queryFactory
                .select(groupOrder)
                .from(groupOrder)
                .where(
                        groupOrderEqType(type),
                        groupOrderLikeSearch(search)
                )
                .orderBy(
                        groupOrderObSort(sort)
                ).fetch();

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
}
