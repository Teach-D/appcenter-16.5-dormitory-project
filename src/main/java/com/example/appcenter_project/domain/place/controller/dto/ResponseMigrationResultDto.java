package com.example.appcenter_project.domain.place.controller.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResponseMigrationResultDto {

    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final int scannedRows;
    private final int matchedRows;
    private final int notFoundRows;
    private final int errorRows;
    private final Long quotaExceededAtRow;
    private final Long lastProcessedId;

    private ResponseMigrationResultDto(LocalDateTime startedAt, LocalDateTime finishedAt,
                                       int scannedRows, int matchedRows, int notFoundRows, int errorRows,
                                       Long quotaExceededAtRow, Long lastProcessedId) {
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.scannedRows = scannedRows;
        this.matchedRows = matchedRows;
        this.notFoundRows = notFoundRows;
        this.errorRows = errorRows;
        this.quotaExceededAtRow = quotaExceededAtRow;
        this.lastProcessedId = lastProcessedId;
    }

    public static ResponseMigrationResultDto of(LocalDateTime startedAt, LocalDateTime finishedAt,
                                                 int scannedRows, int matchedRows, int notFoundRows, int errorRows,
                                                 Long quotaExceededAtRow, Long lastProcessedId) {
        return new ResponseMigrationResultDto(startedAt, finishedAt, scannedRows, matchedRows, notFoundRows, errorRows,
                quotaExceededAtRow, lastProcessedId);
    }
}
