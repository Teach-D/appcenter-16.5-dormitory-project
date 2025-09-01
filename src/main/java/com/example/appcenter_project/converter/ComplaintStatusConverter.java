package com.example.appcenter_project.converter;

import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ComplaintStatusConverter implements Converter<String, ComplaintStatus> {
    @Override
    public ComplaintStatus convert(String source) {
        for (ComplaintStatus status : ComplaintStatus.values()) {
            if (status.getDescription().equals(source) || status.name().equalsIgnoreCase(source)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ComplaintStatus: " + source);
    }
}
