package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.domain.place.client.KakaoLocalClient;
import com.example.appcenter_project.domain.place.client.dto.KakaoPlaceDocument;
import com.example.appcenter_project.domain.place.controller.dto.ResponsePlaceSearchItemDto;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private final KakaoLocalClient kakaoLocalClient;
    private final PlaceCacheService placeCacheService;
    private final QuotaGuard quotaGuard;
    private final CacheKeyNormalizer cacheKeyNormalizer;

    public List<ResponsePlaceSearchItemDto> search(String keyword, int size) {
        if (keyword == null || keyword.trim().length() < 2) {
            return List.of();
        }

        String normalizedKey = cacheKeyNormalizer.normalize(keyword);

        java.util.Optional<String> cached = placeCacheService.get(normalizedKey);
        if (cached.isPresent()) {
            String cachedValue = cached.get();
            if (placeCacheService.isNotFoundSentinel(cachedValue)) {
                placeCacheService.incrementHitCounter();
                return List.of();
            }
            placeCacheService.incrementHitCounter();
        }

        if (quotaGuard.isExceeded()) {
            throw new CustomException(ErrorCode.KAKAO_QUOTA_EXCEEDED);
        }

        quotaGuard.increment();
        List<KakaoPlaceDocument> documents = kakaoLocalClient.searchTop(normalizedKey, size);
        placeCacheService.incrementMissCounter();

        if (documents.isEmpty()) {
            placeCacheService.putNotFound(normalizedKey);
            return List.of();
        }

        placeCacheService.put(normalizedKey, documents.get(0).getId());

        return documents.stream()
                .map(doc -> new ResponsePlaceSearchItemDto(
                        doc.getId(),
                        doc.getPlaceName(),
                        doc.getRoadAddressName(),
                        parseDouble(doc.getY()),
                        parseDouble(doc.getX())
                ))
                .toList();
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
