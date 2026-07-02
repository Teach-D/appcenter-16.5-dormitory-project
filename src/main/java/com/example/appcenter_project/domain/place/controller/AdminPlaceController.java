package com.example.appcenter_project.domain.place.controller;

import com.example.appcenter_project.domain.place.controller.dto.ResponseMigrationResultDto;
import com.example.appcenter_project.domain.place.controller.dto.ResponsePlaceStatsDto;
import com.example.appcenter_project.domain.place.service.PlaceMigrationService;
import com.example.appcenter_project.domain.place.service.PlaceStatsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/places")
@Validated
public class AdminPlaceController {

    private final PlaceMigrationService placeMigrationService;
    private final PlaceStatsService placeStatsService;

    @PostMapping("/migrate")
    public ResponseEntity<ResponseMigrationResultDto> migrate(
            @RequestParam(defaultValue = "10000") @Min(1) @Max(10000) int maxRows,
            @RequestParam(defaultValue = "500") @Min(100) @Max(1000) int chunkSize,
            @RequestParam(defaultValue = "100") @Min(0) @Max(1000) int throttleMs) {
        ResponseMigrationResultDto result = placeMigrationService.migrate(maxRows, chunkSize, throttleMs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<ResponsePlaceStatsDto> getStats(
            @RequestParam(defaultValue = "7") @Min(1) @Max(30) int days) {
        return ResponseEntity.ok(placeStatsService.getStats(days));
    }
}
