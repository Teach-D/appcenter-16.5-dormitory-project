package com.example.appcenter_project.domain.place.service;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.place.entity.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceGroupOrderUpdateService {

    private final GroupOrderRepository groupOrderRepository;

    @Transactional
    public void updatePlace(Long groupOrderId, Place place, String rawPlaceName) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId).orElse(null);
        if (groupOrder != null && groupOrder.getPlace() == null) {
            groupOrder.assignPlace(place, rawPlaceName);
        }
    }
}
