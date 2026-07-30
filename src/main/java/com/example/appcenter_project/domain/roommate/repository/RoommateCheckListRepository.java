package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoommateCheckListRepository extends JpaRepository<RoommateCheckList,Long> {
    Optional<RoommateCheckList> findByUserId(Long userId);

    Optional<RoommateCheckList> findFirstByUserIdAndYearAndSemester(
            Long userId,
            Integer year,
            SemesterType semester
    );

    // 내가 작성한 체크리스트 최신순 (이전 학기 필터링용)
    List<RoommateCheckList> findByUserIdOrderByIdDesc(Long userId);
}