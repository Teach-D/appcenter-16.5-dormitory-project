package com.example.appcenter_project.mapper;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TipMapper {

    List<ResponseTipDto> findTips();
}
