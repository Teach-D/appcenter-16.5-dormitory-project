package com.example.appcenter_project.domain.place.controller.dto;

import lombok.Getter;

@Getter
public class ResponsePlaceSearchItemDto {

    private final String placeId;
    private final String placeName;
    private final String roadAddress;
    private final double latitude;
    private final double longitude;

    public ResponsePlaceSearchItemDto(String placeId, String placeName, String roadAddress,
                                      double latitude, double longitude) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
