package com.example.appcenter_project.utils;

import com.example.appcenter_project.aspect.log.LogTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TraceLogger {

    private final LogTrace logTrace;

    public void info(String message) {
        String traceId = getCurrentTraceId();
        String indentation = getCurrentIndentation();
        log.info("[{}] {}{}", traceId, indentation, message);
    }

    private String getCurrentTraceId() {
        try {
            java.lang.reflect.Field traceIdHolderField = logTrace.getClass().getDeclaredField("traceIdHolder");
            traceIdHolderField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            ThreadLocal<com.example.appcenter_project.aspect.log.TraceId> traceIdHolder = 
                (ThreadLocal<com.example.appcenter_project.aspect.log.TraceId>) traceIdHolderField.get(logTrace);
            
            com.example.appcenter_project.aspect.log.TraceId traceId = traceIdHolder.get();
            return traceId != null ? traceId.getId() : "no-trace";
        } catch (Exception e) {
            return "no-trace";
        }
    }

    private String getCurrentIndentation() {
        try {
            java.lang.reflect.Field traceIdHolderField = logTrace.getClass().getDeclaredField("traceIdHolder");
            traceIdHolderField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            ThreadLocal<com.example.appcenter_project.aspect.log.TraceId> traceIdHolder = 
                (ThreadLocal<com.example.appcenter_project.aspect.log.TraceId>) traceIdHolderField.get(logTrace);
            
            com.example.appcenter_project.aspect.log.TraceId traceId = traceIdHolder.get();
            if (traceId == null) {
                return "";
            }
            
            return addSpace("", traceId.getLevel());
        } catch (Exception e) {
            return "";
        }
    }

    private String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("|   ");
        }
        return sb.toString();
    }
}
