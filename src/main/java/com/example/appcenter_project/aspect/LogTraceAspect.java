package com.example.appcenter_project.aspect;

import com.example.appcenter_project.aspect.log.LogTrace;
import com.example.appcenter_project.aspect.log.TraceStatus;
import com.example.appcenter_project.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
@RequiredArgsConstructor
public class LogTraceAspect {

    private final LogTrace logTrace;

    @Around("execution(* com.example.appcenter_project..*(..)) " +
            "&& !within(com.example.appcenter_project.aspect.log.*)" +
            "&& !within(com.example.appcenter_project.config.*)" +
            "&& !within(com.example.appcenter_project.utils.*)" +
            "&& !within(com.example.appcenter_project.security.*)" +
            "&& !within(com.example.appcenter_project.security..*)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String packageName = joinPoint.getSignature().getDeclaringType().getPackage().getName();
        
        // CrudRepository 메서드이고 CustomUserDetailsService에서 호출된 경우 로깅 건너뛰기
        if (isCrudRepositoryMethod(className, methodName) && isCalledFromCustomUserDetailsService()) {
            return joinPoint.proceed(); // 로깅 없이 바로 실행
        }
        
        boolean isController = packageName.contains(".controller");
        
        String displayName;
        if (isController) {
            String params = getParametersInfo(joinPoint);
            displayName = className + "." + methodName + " | userId : " + params;
        } else {
            displayName = className + "." + methodName;
        }
        
        TraceStatus begin = logTrace.begin(displayName);
        try {
            Object result = joinPoint.proceed();
            logTrace.end(begin);
            return result;
        } catch (Exception e) {
            logTrace.exception(begin, e);
            throw e;
        }
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
    

    private boolean isCrudRepositoryMethod(String className, String methodName) {
        return className.equals("CrudRepository") || 
               (className.contains("Repository") && 
                (methodName.equals("findById") || methodName.equals("save") || 
                 methodName.equals("delete") || methodName.equals("findAll")));
    }

    private boolean isCalledFromCustomUserDetailsService() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("CustomUserDetailsService")) {
                return true;
            }
        }
        return false;
    }
}
