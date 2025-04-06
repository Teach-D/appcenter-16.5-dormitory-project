package com.example.appcenter_project.enums.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DormType {
    DORM_2("2기숙사"),
    DORM_3("3기숙사");

    private final String description;
}