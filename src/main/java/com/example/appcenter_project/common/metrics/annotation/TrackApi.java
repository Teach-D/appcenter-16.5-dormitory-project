package com.example.appcenter_project.common.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 호출 통계를 추적하기 위한 애너테이션.
 * 컨트롤러 클래스 또는 메서드에 적용 가능.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackApi {
}