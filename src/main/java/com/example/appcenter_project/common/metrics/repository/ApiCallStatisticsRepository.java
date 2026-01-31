package com.example.appcenter_project.common.metrics.repository;

import com.example.appcenter_project.common.metrics.entity.ApiCallStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ApiCallStatisticsRepository extends JpaRepository<ApiCallStatistics, Long> {

    Optional<ApiCallStatistics> findByCallDateAndApiPathAndHttpMethod(
            LocalDate callDate, String apiPath, String httpMethod);

    List<ApiCallStatistics> findByCallDate(LocalDate callDate);

    List<ApiCallStatistics> findByCallDateBetween(LocalDate from, LocalDate to);

    List<ApiCallStatistics> findByApiPathAndCallDateBetween(
            String apiPath, LocalDate from, LocalDate to);

    @Query("SELECT a FROM ApiCallStatistics a WHERE a.callDate BETWEEN :from AND :to ORDER BY a.callDate DESC, a.callCount DESC")
    List<ApiCallStatistics> findAllByDateRangeOrderByCallCountDesc(
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT SUM(a.callCount) FROM ApiCallStatistics a WHERE a.apiPath = :apiPath AND a.callDate BETWEEN :from AND :to")
    Long sumCallCountByApiPathAndDateRange(
            @Param("apiPath") String apiPath,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}