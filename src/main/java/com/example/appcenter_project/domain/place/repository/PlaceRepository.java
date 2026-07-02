package com.example.appcenter_project.domain.place.repository;

import com.example.appcenter_project.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByPlaceId(String placeId);

    boolean existsByPlaceId(String placeId);
}
