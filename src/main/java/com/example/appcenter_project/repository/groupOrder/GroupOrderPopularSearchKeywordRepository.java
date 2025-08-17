package com.example.appcenter_project.repository.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrderPopularSearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupOrderPopularSearchKeywordRepository extends JpaRepository<GroupOrderPopularSearchKeyword, Long> {

    List<GroupOrderPopularSearchKeyword> findTop10ByOrderBySearchCountDesc();

    boolean existsByKeyword(String keyword);

    GroupOrderPopularSearchKeyword findByKeyword(String keyword);
}
