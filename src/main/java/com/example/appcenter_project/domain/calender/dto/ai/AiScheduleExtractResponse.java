package com.example.appcenter_project.domain.calender.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
public class AiScheduleExtractResponse {
    private String status;
    private Integer count;
    private List<AiScheduleExtractItem> data;
}