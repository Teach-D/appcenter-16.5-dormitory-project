package com.example.appcenter_project.common.metrics.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "api_call_statistics",
    uniqueConstraints = @UniqueConstraint(columnNames = {"call_date", "api_path", "http_method"})
)
public class ApiCallStatistics extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "call_date", nullable = false)
    private LocalDate callDate;

    @Column(name = "api_path", nullable = false, length = 255)
    private String apiPath;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "call_count", nullable = false)
    private Long callCount;

    @Column(name = "last_called_at")
    private LocalDateTime lastCalledAt;

    @Builder
    public ApiCallStatistics(LocalDate callDate, String apiPath, String httpMethod) {
        this.callDate = callDate;
        this.apiPath = apiPath;
        this.httpMethod = httpMethod;
        this.callCount = 1L;
        this.lastCalledAt = LocalDateTime.now();
    }

    public void incrementCallCount() {
        this.callCount++;
        this.lastCalledAt = LocalDateTime.now();
    }
}