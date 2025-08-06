package com.example.appcenter_project.repository.calender;

import com.example.appcenter_project.entity.calender.Calender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalenderRepository extends JpaRepository<Calender,Long> {

    // 특정 년월에 해당하는 캘린더 조회 (시작일 또는 종료일이 해당 월에 포함되는 경우)
    @Query("SELECT c FROM Calender c WHERE " +
            "(c.startDate >= :startOfMonth AND c.startDate < :startOfNextMonth) OR " +
            "(c.endDate >= :startOfMonth AND c.endDate < :startOfNextMonth) OR " +
            "(c.startDate < :startOfMonth AND c.endDate >= :startOfNextMonth)")
    List<Calender> findByYearAndMonth(@Param("startOfMonth") LocalDate startOfMonth,
                                      @Param("startOfNextMonth") LocalDate startOfNextMonth);
}
