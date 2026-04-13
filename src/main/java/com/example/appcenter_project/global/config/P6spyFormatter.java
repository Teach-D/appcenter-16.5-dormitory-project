package com.example.appcenter_project.global.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class P6spyFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        if (sql == null || sql.isBlank()) {
            return "";
        }
        return String.format("[P6Spy] %s | %dms | %s",
                category.toUpperCase(), elapsed, sql.trim());
    }
}
