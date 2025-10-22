package com.example.appcenter_project.domain.tip.mapper;

import com.example.appcenter_project.domain.tip.dto.response.ResponseTipDetailDto;
import com.example.appcenter_project.domain.tip.dto.response.ResponseTipDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TipMapper {

    List<ResponseTipDto> findTips();
    ResponseTipDetailDto findTip(@Param("tipId") Long tipId);
    List<ResponseTipDto> findLikeTips(@Param("userId") Long userId);
    List<ResponseTipDto> findTipsByUserId(@Param("userId") Long userId);
}
