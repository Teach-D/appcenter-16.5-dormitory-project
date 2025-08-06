package com.example.appcenter_project.repository.report;

import com.example.appcenter_project.entity.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
