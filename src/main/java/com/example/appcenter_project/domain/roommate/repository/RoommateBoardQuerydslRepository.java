package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;

import java.util.List;

public interface RoommateBoardQuerydslRepository {
    List<RoommateBoard> searchBoards(Long lastId, int size, String keyword, Integer year, SemesterType semester);
}
