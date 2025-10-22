package com.example.appcenter_project.domain.groupOrder.mapper;

import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderDto;
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
