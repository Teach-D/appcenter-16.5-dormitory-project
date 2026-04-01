package com.example.appcenter_project.global.aspect.distributedlock;

import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private static final String LOCK_KEY_PREFIX = "distributed-lock:";

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.example.appcenter_project.global.aspect.distributedlock.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);

        String lockKey = LOCK_KEY_PREFIX + resolveKey(distributedLock.key(), signature, joinPoint.getArgs());
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            // leaseTime 미지정 → Redisson Watchdog 자동 활성화 (기본 30초, 10초마다 자동 갱신)
            locked = lock.tryLock(distributedLock.waitTime(), distributedLock.timeUnit());
            if (!locked) {
                log.warn("[DistributedLock] 락 획득 실패 - key: {}", lockKey);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }
            log.debug("[DistributedLock] 락 획득 성공 - key: {}", lockKey);
            return joinPoint.proceed();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[DistributedLock] 락 해제 - key: {}", lockKey);
            }
        }
    }

    private String resolveKey(String keyExpression, MethodSignature signature, Object[] args) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = signature.getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
