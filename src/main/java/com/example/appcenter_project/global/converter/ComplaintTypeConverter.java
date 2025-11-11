package com.example.appcenter_project.global.converter;

import com.example.appcenter_project.domain.complaint.enums.ComplaintType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ComplaintTypeConverter implements Converter<String, ComplaintType> {
    @Override
    public ComplaintType convert(String source) {
        for (ComplaintType type : ComplaintType.values()) {
            if (type.getDescription().equals(source) || type.name().equalsIgnoreCase(source)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ComplaintType: " + source);
    }
}
