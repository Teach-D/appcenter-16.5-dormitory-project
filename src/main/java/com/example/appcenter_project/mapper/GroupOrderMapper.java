package com.example.appcenter_project.mapper;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupOrderMapper {
    List<ResponseGroupOrderDto> findGroupOrders(@Param("groupOrderType") String groupOrderType,
                                                @Param("search") String search,
                                                @Param("sort") String sort);

/*    ResponseGroupOrderDetailDto findGroupOrderById(@Param("groupOrderId") Long groupOrderId);

    List<ResponseGroupOrderDto> findLikeGroupOrders(@Param("userId") Long userId);
    List<ResponseGroupOrderDto> findGroupOrdersByUserId(@Param("userId") Long userId);*/

}
