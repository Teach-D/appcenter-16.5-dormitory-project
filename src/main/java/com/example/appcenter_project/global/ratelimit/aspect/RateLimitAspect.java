package com.example.appcenter_project.global.ratelimit.aspect;

import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.ratelimit.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        if (request != null) {
            String clientIp = resolveClientIp(request);
            String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String key = "rate_limit:" + className + ":" + methodName + ":" + clientIp;

            Long count = stringRedisTemplate.opsForValue().increment(key);

            if (count != null && count == 1L) {
                stringRedisTemplate.expire(key, rateLimit.windowSeconds(), TimeUnit.SECONDS);
            }

            if (count != null && count > rateLimit.maxRequests()) {
                log.warn("RateLimit 초과 Brute force 공격 의심 - IP: {}, 엔드포인트: {}.{}(), 횟수: {}/{}",
                        clientIp, className, methodName, count, rateLimit.maxRequests());
                throw new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED);
            }
        }

        return joinPoint.proceed();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
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
