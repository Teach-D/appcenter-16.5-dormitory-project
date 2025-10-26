package com.example.appcenter_project.domain.calender.repository;

import com.example.appcenter_project.domain.calender.entity.Calender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalenderRepository extends JpaRepository<Calender,Long> {

    @Query("SELECT c FROM Calender c WHERE " +
            "(c.startDate >= :startOfMonth AND c.startDate < :startOfNextMonth) OR " +
            "(c.endDate >= :startOfMonth AND c.endDate < :startOfNextMonth) OR " +
            "(c.startDate < :startOfMonth AND c.endDate >= :startOfNextMonth)")
    List<Calender> findByMonthBetween(@Param("startOfMonth") LocalDate startOfMonth,
                                      @Param("startOfNextMonth") LocalDate startOfNextMonth);
}
