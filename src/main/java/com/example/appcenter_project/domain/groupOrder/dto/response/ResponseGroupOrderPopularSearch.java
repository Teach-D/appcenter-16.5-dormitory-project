package com.example.appcenter_project.domain.groupOrder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseGroupOrderPopularSearch {

    private int ranking;
    private String keyword;
}
