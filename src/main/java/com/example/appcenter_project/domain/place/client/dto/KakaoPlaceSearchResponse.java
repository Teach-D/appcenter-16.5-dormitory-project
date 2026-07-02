package com.example.appcenter_project.domain.place.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoPlaceSearchResponse {

    private List<KakaoPlaceDocument> documents;
}
