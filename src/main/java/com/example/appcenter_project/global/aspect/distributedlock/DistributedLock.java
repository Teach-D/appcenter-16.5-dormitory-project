package com.example.appcenter_project.global.aspect.distributedlock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * Redis 락 키. SpEL 표현식 지원.
     * 예: "'coupon:' + #couponId"
     */
    String key();

    /**
     * 락 획득 최대 대기 시간 (기본 5초).
     * leaseTime을 지정하지 않아 Redisson Watchdog이 자동으로 락을 갱신함.
     */
    long waitTime() default 5L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
