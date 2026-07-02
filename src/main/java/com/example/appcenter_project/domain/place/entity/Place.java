package com.example.appcenter_project.domain.place.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.place.client.dto.KakaoPlaceDocument;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String placeId;

    @Column(nullable = false)
    private String placeName;

    private String roadAddress;

    private Double latitude;

    private Double longitude;

    public static Place ofKakao(KakaoPlaceDocument document) {
        Place place = new Place();
        place.placeId = document.getId();
        place.placeName = document.getPlaceName();
        place.roadAddress = document.getRoadAddressName();
        place.latitude = parseCoordinate(document.getY());
        place.longitude = parseCoordinate(document.getX());
        return place;
    }

    public static Place ofDirect(String placeId, String placeName, String roadAddress, Double latitude, Double longitude) {
        Place place = new Place();
        place.placeId = placeId;
        place.placeName = placeName;
        place.roadAddress = roadAddress;
        place.latitude = latitude;
        place.longitude = longitude;
        return place;
    }

    private static Double parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
