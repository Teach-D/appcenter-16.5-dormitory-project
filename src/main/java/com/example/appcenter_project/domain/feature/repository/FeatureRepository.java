package com.example.appcenter_project.domain.feature.repository;

import com.example.appcenter_project.domain.feature.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
    Optional<Feature> findByKey(String key);
}
