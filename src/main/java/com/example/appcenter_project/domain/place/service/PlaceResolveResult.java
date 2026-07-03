package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.domain.place.entity.Place;
import com.example.appcenter_project.domain.place.enums.NormalizationOutcome;
import lombok.Getter;

@Getter
public class PlaceResolveResult {

    private final Place place;
    private final NormalizationOutcome outcome;

    private PlaceResolveResult(Place place, NormalizationOutcome outcome) {
        this.place = place;
        this.outcome = outcome;
    }

    public static PlaceResolveResult of(Place place, NormalizationOutcome outcome) {
        return new PlaceResolveResult(place, outcome);
    }
}
