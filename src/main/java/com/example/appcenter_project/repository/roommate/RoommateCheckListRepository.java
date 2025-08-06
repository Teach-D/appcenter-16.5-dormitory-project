package com.example.appcenter_project.repository.roommate;

import com.example.appcenter_project.entity.roommate.RoommateCheckList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoommateCheckListRepository extends JpaRepository<RoommateCheckList,Long> {
    Optional<RoommateCheckList> findByUserId(Long userId);
}
