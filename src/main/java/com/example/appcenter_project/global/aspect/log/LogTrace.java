package com.example.appcenter_project.global.aspect.log;

public interface LogTrace {

    Long begin(String message);
    Long begin(String message, String userId);
    void end(Long startTimeMs, String message);
    void exception(Long startTimeMs, String message, Exception e);
}