package com.example.appcenter_project.global.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer 메트릭 수집 설정
 * - @Timed 애노테이션 지원
 * - Custom 비즈니스 메트릭 수집
 */
@Configuration
public class MetricsConfig {

    /**
     * @Timed 애노테이션을 통한 메서드 실행 시간 측정을 활성화
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry meterRegistry) {
        return new CountedAspect(meterRegistry);
    }
}