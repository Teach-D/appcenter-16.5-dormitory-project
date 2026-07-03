package com.example.appcenter_project.domain.place.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoPlaceDocument {

    private String id;

    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("road_address_name")
    private String roadAddressName;

    private String x;

    private String y;
}
