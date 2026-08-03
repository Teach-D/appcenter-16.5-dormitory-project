package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;

public record MatchingPeriod(int year, SemesterType semester, RoommateMatchingStatus status) {
}