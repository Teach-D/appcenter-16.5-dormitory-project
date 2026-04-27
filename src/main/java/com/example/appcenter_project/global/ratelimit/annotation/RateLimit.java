package com.example.appcenter_project.global.ratelimit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    int limit() default 1;

    long window() default 5;

    TimeUnit unit() default TimeUnit.SECONDS;

    String keyPrefix() default "rate_limit";
}
