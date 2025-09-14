package com.example.appcenter_project.aspect.log;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.UUID;

@Slf4j
public class ThreadLocalLogTrace implements LogTrace {

    private static final Logger traceLogger = LoggerFactory.getLogger("TRACE_LOGGER");

    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EX_PREFIX = "<X-";

    @Override
    public Long begin(String message) {
        syncTraceId();

        String traceLevel = MDC.get("traceLevel");
        String traceId = MDC.get("traceId");
        String userId = MDC.get("userId");

        int level = 0;
        try {
            level = Integer.parseInt(traceLevel);
        } catch (NumberFormatException e) {
            level = 0;
            MDC.put("traceLevel", "0");
        }

        Long startTimeMs = System.currentTimeMillis();

        traceLogger.info("[{}] [{}] | {}{}", traceId, userId, addSpace(START_PREFIX, level), message);

        return startTimeMs;
    }

    @Override
    public Long begin(String message, String userId) {
        syncTraceId();

        String traceLevel = MDC.get("traceLevel");
        String traceId = MDC.get("traceId");
        MDC.put("userId", userId);

        int level = 0;
        try {
            level = Integer.parseInt(traceLevel);
        } catch (NumberFormatException e) {
            level = 0;
            MDC.put("traceLevel", "0");
        }

        Long startTimeMs = System.currentTimeMillis();

        traceLogger.info("[{}] [{}] | {}{}", traceId, userId, addSpace(START_PREFIX, level), message);

        return startTimeMs;
    }


    @Override
    public void end(Long startTimeMs, String message) {
        complete(startTimeMs, message,null);
    }

    @Override
    public void exception(Long startTimeMs, String message, Exception e) {
        complete(startTimeMs, message, e);
    }

    private void complete(Long startTimeMs, String message, Exception e) {
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - startTimeMs;

        String traceLevel = MDC.get("traceLevel");
        String traceId = MDC.get("traceId");
        String userId = MDC.get("userId");

        int level = 0;
        try {
            level = Integer.parseInt(traceLevel);
        } catch (NumberFormatException ex) {
            level = 0;
        }

        if (e == null) {
            traceLogger.info("[{}] [{}] | {}{} time={}ms", traceId, userId, addSpace(COMPLETE_PREFIX, level), message,
                    resultTimeMs);
        }
        else {
            traceLogger.info("[{}] [{}] | {}{} time={}ms ex={}", traceId, userId, addSpace(EX_PREFIX, level), message, resultTimeMs,
                    e.toString());
        }
        releaseTraceId();
    }

    private void syncTraceId() {
        String traceId = MDC.get("traceId");
        String traceLevel = MDC.get("traceLevel");

        if (traceId == null || traceLevel == null) {
            // traceId나 traceLevel 중 하나라도 없으면 새로 시작
            MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
            MDC.put("traceLevel", "0");
        }
        else {
            try {
                int currentLevel = Integer.parseInt(traceLevel);
                int newLevel = currentLevel + 1;
                MDC.put("traceLevel", String.valueOf(newLevel));
            } catch (NumberFormatException e) {
                MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
                MDC.put("traceLevel", "0");
            }
        }
    }

    private void releaseTraceId() {
        String traceLevel = MDC.get("traceLevel");
        if (traceLevel == null) {
            MDC.clear();
            return;
        }
        
        try {
            if (Objects.equals(traceLevel, "0")) {
                MDC.remove("traceId");
                MDC.remove("traceLevel");
                MDC.remove("userId");
            }
            else {
                int currentLevel = Integer.parseInt(traceLevel);
                int newLevel = currentLevel - 1;
                MDC.put("traceLevel", String.valueOf(newLevel));
            }
        } catch (NumberFormatException e) {
            MDC.clear();
        }
    }

    private static String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append( (i == level - 1) ? "|" + prefix : "|   ");
        }
        return sb.toString();
    }

}