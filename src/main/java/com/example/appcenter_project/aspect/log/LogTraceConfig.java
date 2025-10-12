package com.example.appcenter_project.aspect.log;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogTraceConfig {

    @Bean
    public LogTrace logTrace() {
        //return new FieldLogTrace();
        return new ThreadLocalLogTrace();
    }
}