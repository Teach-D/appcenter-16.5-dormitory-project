package com.example.appcenter_project.global.ratelimit;

import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.ratelimit.aspect.RateLimitAspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(joinPoint.getSignature()).willReturn(methodSignature);
        given(methodSignature.getName()).willReturn("findCoupon");
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("첫 번째 요청은 정상 처리된다")
    void first_request_passes() throws Throwable {
        Method method = SampleController.class.getMethod("findCoupon");
        given(methodSignature.getMethod()).willReturn(method);
        given(valueOperations.increment(anyString())).willReturn(1L);
        given(joinPoint.proceed()).willReturn("OK");

        Object result = rateLimitAspect.checkRateLimit(joinPoint);

        assertThat(result).isEqualTo("OK");
        verify(redisTemplate).expire(anyString(), eq(5L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("limit 초과 요청은 RATE_LIMIT_EXCEEDED 예외가 발생한다")
    void exceed_limit_throws_exception() throws Throwable {
        Method method = SampleController.class.getMethod("findCoupon");
        given(methodSignature.getMethod()).willReturn(method);
        given(valueOperations.increment(anyString())).willReturn(2L); // limit=1 초과

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.RATE_LIMIT_EXCEEDED));

        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("두 번째 요청부터는 Redis TTL을 새로 설정하지 않는다")
    void no_expire_reset_after_first_request() throws Throwable {
        Method method = SampleController.class.getMethod("findCoupon");
        given(methodSignature.getMethod()).willReturn(method);
        given(valueOperations.increment(anyString())).willReturn(2L);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint))
                .isInstanceOf(CustomException.class);

        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
    }

    /**
     * @RateLimit 어노테이션이 붙은 메서드를 테스트 대역으로 사용
     */
    static class SampleController {
        @com.example.appcenter_project.global.ratelimit.annotation.RateLimit(limit = 1, window = 5, unit = TimeUnit.SECONDS)
        public String findCoupon() {
            return "OK";
        }
    }
}
