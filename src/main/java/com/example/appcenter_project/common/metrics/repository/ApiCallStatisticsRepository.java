package com.example.appcenter_project.common.metrics.repository;

import com.example.appcenter_project.common.metrics.entity.ApiCallStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiCallStatisticsRepository extends JpaRepository<ApiCallStatistics, Long> {

    /**
     * API 이름으로 통계 조회
     */
    Optional<ApiCallStatistics> findByApiName(String apiName);

    /**
     * API 이름으로 총 호출 횟수 조회
     */
    @Query("SELECT a.totalCalls FROM ApiCallStatistics a WHERE a.apiName = :apiName")
    Optional<Long> findTotalCallsByApiName(String apiName);
}