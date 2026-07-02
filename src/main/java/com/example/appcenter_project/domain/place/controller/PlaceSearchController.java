package com.example.appcenter_project.domain.place.controller;

import com.example.appcenter_project.domain.place.controller.dto.ResponsePlaceSearchItemDto;
import com.example.appcenter_project.domain.place.service.PlaceSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
@Validated
public class PlaceSearchController {

    private final PlaceSearchService placeSearchService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam @NotBlank @Size(max = 50) String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(15) int size) {
        List<ResponsePlaceSearchItemDto> results = placeSearchService.search(keyword, size);
        return ResponseEntity.ok(Map.of("results", results));
    }
}
