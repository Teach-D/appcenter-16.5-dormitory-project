package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoommateCheckListRepository extends JpaRepository<RoommateCheckList,Long> {
    Optional<RoommateCheckList> findByUserId(Long userId);

    // 내가 작성한 가장 최근(현재 학기) 체크리스트
    Optional<RoommateCheckList> findFirstByUserIdOrderByIdDesc(Long userId);

    // 내가 작성한 체크리스트 최신순 (이전 학기 필터링용)
    List<RoommateCheckList> findByUserIdOrderByIdDesc(Long userId);
}