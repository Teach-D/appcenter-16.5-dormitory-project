package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderSort;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;

import java.util.List;

public interface GroupOrderQuerydslRepository {

    List<GroupOrder> findGroupOrdersComplex(GroupOrderSort sort, GroupOrderType type, String search);
}
