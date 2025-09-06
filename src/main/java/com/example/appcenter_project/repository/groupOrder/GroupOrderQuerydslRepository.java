package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface GroupOrderQuerydslRepository {

    List<GroupOrder> findGroupOrdersComplex(GroupOrderSort sort, GroupOrderType type, String search);
}
