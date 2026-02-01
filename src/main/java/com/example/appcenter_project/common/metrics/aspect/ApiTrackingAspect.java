package com.example.appcenter_project.common.metrics.aspect;

import com.example.appcenter_project.common.metrics.annotation.TrackApi;
import com.example.appcenter_project.common.metrics.service.ApiCallStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiTrackingAspect {

    private final ApiCallStatisticsService statisticsService;

    @Around("@annotation(trackApi) || @within(trackApi)")
    public Object trackApiCall(ProceedingJoinPoint joinPoint, TrackApi trackApi) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        if (request != null) {
            String apiPath = normalizeApiPath(request.getRequestURI());
            String httpMethod = request.getMethod();

            statisticsService.recordApiCall(apiPath, httpMethod);
        }

        return joinPoint.proceed();
    }

    private String normalizeApiPath(String path) {
        return path.replaceAll("/\\d+", "/**");
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("HttpServletRequest를 가져올 수 없습니다.", e);
            return null;
        }
    }
}