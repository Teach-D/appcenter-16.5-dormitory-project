package com.example.appcenter_project.domain.groupOrder.repository;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderPopularSearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupOrderPopularSearchKeywordRepository extends JpaRepository<GroupOrderPopularSearchKeyword, Long> {

    List<GroupOrderPopularSearchKeyword> findTop10ByOrderBySearchCountDesc();

    boolean existsByKeyword(String keyword);

    GroupOrderPopularSearchKeyword findByKeyword(String keyword);
}
