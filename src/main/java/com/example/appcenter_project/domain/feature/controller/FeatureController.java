package com.example.appcenter_project.domain.feature.controller;

import com.example.appcenter_project.domain.feature.dto.request.RequestFeatureDto;
import com.example.appcenter_project.domain.feature.dto.response.ResponseFeatureDto;
import com.example.appcenter_project.domain.feature.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/features")
public class FeatureController {

    private final FeatureService featureService;

    @PostMapping
    public ResponseEntity<Void> saveFeature(@RequestBody RequestFeatureDto dto) {
        featureService.saveFeature(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{key}")
    public ResponseEntity<ResponseFeatureDto> findFeature(@PathVariable String key) {
        return ResponseEntity.status(HttpStatus.OK).body(featureService.findFeature(key));
    }

    @GetMapping
    public ResponseEntity<List<ResponseFeatureDto>> findAllFeatures() {
        return ResponseEntity.status(HttpStatus.OK).body(featureService.findAllFeatures());
    }

    @PutMapping
    public ResponseEntity<Void> updateFeature(@RequestBody RequestFeatureDto dto) {
        featureService.updateFeature(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
