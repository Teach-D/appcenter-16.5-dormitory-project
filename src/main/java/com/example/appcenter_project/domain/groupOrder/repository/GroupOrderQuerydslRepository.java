package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.dto.response.GroupOrderListProjection;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderSort;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupOrderQuerydslRepository {

    List<GroupOrderListProjection> findGroupOrdersComplex(GroupOrderSort sort, GroupOrderType type, String search, Pageable pageable);

    void bulkMarkExpired(List<Long> ids);

    List<GroupOrder> findUnnormalizedFoodOrders(int pageSize, Long lastId);

    long countDistinctRawPlaceName();

    long countDistinctPlaceId();

    long countByCreatedAtAfter(LocalDateTime since);

    long countByCreatedAtAfterAndPlaceIsNull(LocalDateTime since);
}
