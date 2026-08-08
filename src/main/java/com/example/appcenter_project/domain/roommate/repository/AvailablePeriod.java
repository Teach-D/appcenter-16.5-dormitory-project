package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.enums.SemesterType;

public record AvailablePeriod(Integer year, SemesterType semester) {
}