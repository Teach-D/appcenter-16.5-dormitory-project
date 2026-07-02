package com.example.appcenter_project.domain.place.service;

import org.springframework.stereotype.Component;

@Component
public class CacheKeyNormalizer {

    public String normalize(String keyword) {
        if (keyword == null) {
            return null;
        }
        return keyword.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
