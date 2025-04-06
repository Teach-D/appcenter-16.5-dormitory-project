package com.example.appcenter_project.enums.groupOrder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupOrderType {

    ALL("전체"),
    DELIVERY("배달"),
    GROCERY("식료품"),
    LIFE_ITEM("생활용품"),
    ETC("기타");

    private final String description;
}
