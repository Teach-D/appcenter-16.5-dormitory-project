package com.example.appcenter_project.domain.fcm.converter;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Set;

@Converter
public class FcmRoutingTypeConverter implements AttributeConverter<FcmRoutingType, String> {

    private static final Set<String> LEGACY_VALUES = Set.of("CHAT", "NOTICE");

    @Override
    public String convertToDatabaseColumn(FcmRoutingType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public FcmRoutingType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        if (LEGACY_VALUES.contains(dbData)) {
            return null;
        }
        try {
            return FcmRoutingType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
