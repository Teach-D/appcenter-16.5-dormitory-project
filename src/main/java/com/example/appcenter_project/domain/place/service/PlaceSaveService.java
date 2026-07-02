package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.domain.place.client.dto.KakaoPlaceDocument;
import com.example.appcenter_project.domain.place.entity.Place;
import com.example.appcenter_project.domain.place.repository.PlaceRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceSaveService {

    private final PlaceRepository placeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Place saveInNewTransaction(KakaoPlaceDocument document) {
        try {
            return placeRepository.save(Place.ofKakao(document));
        } catch (DataIntegrityViolationException e) {
            return placeRepository.findByPlaceId(document.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.KAKAO_API_ERROR));
        }
    }
}
