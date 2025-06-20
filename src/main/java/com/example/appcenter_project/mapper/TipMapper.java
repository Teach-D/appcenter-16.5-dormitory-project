package com.example.appcenter_project.mapper;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TipMapper {

    List<ResponseTipDto> findTips();
    ResponseTipDetailDto findTip(@Param("tipId") Long tipId);
    List<ResponseTipDto> findLikeTips(@Param("userId") Long userId);

}
