package com.example.appcenter_project.global.aspect;

import com.example.appcenter_project.global.aspect.log.LogTrace;
import com.example.appcenter_project.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LogTraceAspect {

    private final LogTrace logTrace;

    /**
     * controller용 aop
     * userId를 begin에 파라미터로 포함
     * 모든 controller의 메서드는 CustomUserDetails가 첫번째 파라미터여야 함
     */
    @Around("execution(* com.example.appcenter_project.controller..*(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String userId = getParametersInfo(joinPoint);

        String displayName = className + "." + methodName;

        Long startTime = logTrace.begin(displayName, userId);

        try {
            Object proceed = joinPoint.proceed();
            logTrace.end(startTime, displayName);

            return proceed;
        } catch (Exception e) {
            logTrace.exception(startTime, displayName, e);
            throw e;
        }

    }

    // service용 aop
    @Around("execution(* com.example.appcenter_project.service..*(..)) &&" +
            "!execution(* com.example.appcenter_project.service.groupOrder.AsyncViewCountService.flushViewCountDB())"
    )
    public Object aroundService(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();

        String displayName = className + "." + methodName;

        Long startTime = logTrace.begin(displayName);

        try {
            Object proceed = joinPoint.proceed();
            logTrace.end(startTime, displayName);

            return proceed;
        } catch (Exception e) {
            logTrace.exception(startTime, displayName, e);
            throw e;
        }

    }

    // repository용 aop (CustomUserDetailsService에서 호출된 경우 제외)
    @Around("execution(* com.example.appcenter_project.repository..*(..)) && !within(com.example.appcenter_project.global.security.CustomUserDetailsService)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        // CustomUserDetailsService에서 호출된 경우 로그 없이 실행
        if (isCalledFromCustomUserDetailsService()) {
            return joinPoint.proceed();
        }
        
        // 실제 Repository 인터페이스 이름 가져오기
        String className = getActualRepositoryName(joinPoint);
        
        String displayName = className + "." + methodName;

        Long startTime = logTrace.begin(displayName);

        try {
            Object proceed = joinPoint.proceed();
            logTrace.end(startTime, displayName);

            return proceed;
        } catch (Exception e) {
            logTrace.exception(startTime, displayName, e);
            throw e;
        }
    }

    /**
     * CustomUserDetailsService에서 호출되었는지 확인
     */
    private boolean isCalledFromCustomUserDetailsService() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("CustomUserDetailsService")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 실제 Repository 인터페이스 이름을 가져오는 메서드
     * Spring Data JPA 프록시에서 실제 인터페이스 이름 추출
     */
    private String getActualRepositoryName(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        
        if (target != null) {
            // 프록시 객체에서 실제 인터페이스들 확인
            Class<?>[] interfaces = target.getClass().getInterfaces();
            
            for (Class<?> intf : interfaces) {
                String interfaceName = intf.getSimpleName();
                // Repository로 끝나는 우리가 정의한 인터페이스 찾기
                if (interfaceName.endsWith("Repository") && 
                    !interfaceName.equals("JpaRepository") && 
                    !interfaceName.equals("CrudRepository") && 
                    !interfaceName.equals("PagingAndSortingRepository")) {
                    return interfaceName;
                }
            }
        }
        
        // 못 찾으면 기본값 반환
        return joinPoint.getSignature().getDeclaringType().getSimpleName();
    }

    private String getParametersInfo(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }

        Object firstArg = args[0];

        if (!(firstArg instanceof CustomUserDetails)) {
            return "";  // User 클래스가 아니면 빈 문자열 반환
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) firstArg;

        return customUserDetails.getId().toString();
    }
}



