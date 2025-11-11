package com.example.appcenter_project.global.converter;

import com.example.appcenter_project.domain.user.enums.DormType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DormTypeConverter implements Converter<String, DormType> {
    @Override
    public DormType convert(String source) {
        for (DormType type : DormType.values()) {
            if (type.getDescription().equals(source) || type.name().equalsIgnoreCase(source)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid DormType: " + source);
    }
}
