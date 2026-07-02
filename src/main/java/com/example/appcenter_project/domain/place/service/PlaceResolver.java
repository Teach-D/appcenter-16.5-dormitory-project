package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.domain.place.client.KakaoLocalClient;
import com.example.appcenter_project.domain.place.client.dto.KakaoPlaceDocument;
import com.example.appcenter_project.domain.place.entity.Place;
import com.example.appcenter_project.domain.place.enums.NormalizationOutcome;
import com.example.appcenter_project.domain.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceResolver {

    private final KakaoLocalClient kakaoLocalClient;
    private final PlaceCacheService placeCacheService;
    private final QuotaGuard quotaGuard;
    private final PlaceRepository placeRepository;
    private final CacheKeyNormalizer cacheKeyNormalizer;
    private final PlaceSaveService placeSaveService;

    public PlaceResolveResult resolve(String placeId, String rawPlaceName, GroupOrderType groupOrderType) {
        if (groupOrderType != GroupOrderType.FOOD) {
            return PlaceResolveResult.of(null, NormalizationOutcome.SKIPPED);
        }

        if (placeId != null && !placeId.isBlank()) {
            return resolveByPlaceId(placeId);
        }

        if (rawPlaceName == null || rawPlaceName.isBlank()) {
            return PlaceResolveResult.of(null, NormalizationOutcome.SKIPPED);
        }

        return resolveByRawPlaceName(rawPlaceName);
    }

    private PlaceResolveResult resolveByPlaceId(String placeId) {
        Optional<Place> existing = placeRepository.findByPlaceId(placeId);
        if (existing.isPresent()) {
            return PlaceResolveResult.of(existing.get(), NormalizationOutcome.MATCHED);
        }

        try {
            Optional<KakaoPlaceDocument> docOpt = kakaoLocalClient.searchFirst(placeId);
            if (docOpt.isEmpty()) {
                return PlaceResolveResult.of(null, NormalizationOutcome.NOT_FOUND);
            }
            Place saved = placeSaveService.saveInNewTransaction(docOpt.get());
            return PlaceResolveResult.of(saved, NormalizationOutcome.MATCHED);
        } catch (Exception e) {
            log.warn("[PlaceResolver] placeId 경로 카카오 API 실패: {}", e.getMessage());
            return PlaceResolveResult.of(null, NormalizationOutcome.FALLBACK_ERROR);
        }
    }

    private PlaceResolveResult resolveByRawPlaceName(String rawPlaceName) {
        String normalizedKey = cacheKeyNormalizer.normalize(rawPlaceName);

        Optional<String> cached = placeCacheService.get(normalizedKey);
        if (cached.isPresent()) {
            String cachedValue = cached.get();
            if (placeCacheService.isNotFoundSentinel(cachedValue)) {
                placeCacheService.incrementHitCounter();
                return PlaceResolveResult.of(null, NormalizationOutcome.NOT_FOUND);
            }
            placeCacheService.incrementHitCounter();
            Optional<Place> place = placeRepository.findByPlaceId(cachedValue);
            return PlaceResolveResult.of(place.orElse(null), NormalizationOutcome.MATCHED);
        }

        if (quotaGuard.isExceeded()) {
            return PlaceResolveResult.of(null, NormalizationOutcome.QUOTA_EXCEEDED);
        }

        try {
            quotaGuard.increment();
            Optional<KakaoPlaceDocument> docOpt = kakaoLocalClient.searchFirst(normalizedKey);
            placeCacheService.incrementMissCounter();

            if (docOpt.isEmpty()) {
                placeCacheService.putNotFound(normalizedKey);
                return PlaceResolveResult.of(null, NormalizationOutcome.NOT_FOUND);
            }

            KakaoPlaceDocument doc = docOpt.get();
            placeCacheService.put(normalizedKey, doc.getId());

            Optional<Place> existing = placeRepository.findByPlaceId(doc.getId());
            if (existing.isPresent()) {
                return PlaceResolveResult.of(existing.get(), NormalizationOutcome.MATCHED);
            }

            Place saved = placeSaveService.saveInNewTransaction(doc);
            return PlaceResolveResult.of(saved, NormalizationOutcome.MATCHED);

        } catch (Exception e) {
            log.warn("[PlaceResolver] rawPlaceName 경로 카카오 API 실패: {}", e.getMessage());
            placeCacheService.incrementFallbackCounter();
            return PlaceResolveResult.of(null, NormalizationOutcome.FALLBACK_ERROR);
        }
    }
}
