package com.example.appcenter_project.domain.roommate.repository;

import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoommateCheckListRepository extends JpaRepository<RoommateCheckList,Long> {
    Optional<RoommateCheckList> findByUserId(Long userId);
}
