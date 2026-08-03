package com.example.appcenter_project.domain.roommate.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SemesterTypeConverter implements AttributeConverter<SemesterType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SemesterType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public SemesterType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return SemesterType.fromCode(dbData);
    }
}
