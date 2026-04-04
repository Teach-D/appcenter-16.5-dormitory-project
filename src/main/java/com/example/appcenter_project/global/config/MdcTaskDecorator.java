package com.example.appcenter_project.global.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> callerMdcContext = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (callerMdcContext != null) {
                    MDC.setContextMap(callerMdcContext);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
