package com.example.appcenter_project.domain.coupon.service;

import com.example.appcenter_project.domain.coupon.dto.response.ResponseCouponDto;
import com.example.appcenter_project.domain.coupon.entity.Coupon;
import com.example.appcenter_project.domain.coupon.repository.CouponRepository;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.cache.CouponLocalCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponStockCacheTest {

    @InjectMocks
    private CouponService couponService;

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private UserRepository userRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private UserNotificationRepository userNotificationRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private CouponLocalCache couponLocalCache;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("Redis 잔여 수가 0이면 DB 조회 없이 즉시 반환한다")
    void stock_depleted_in_cache_skips_db() {
        User user = mock(User.class);
        given(user.getRole()).willReturn(Role.ROLE_USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNotificationRepository.existsByUserIdAndNotificationType(1L, NotificationType.COUPON))
                .willReturn(false);
        given(valueOperations.get("coupon_stock")).willReturn("0");

        ResponseCouponDto result = couponService.findCoupon(1L);

        assertThat(result.isSuccess()).isFalse();
        verify(couponRepository, never()).count();
    }

    @Test
    @DisplayName("Redis 잔여 수가 음수이면 DB 조회 없이 즉시 반환한다")
    void negative_stock_cache_skips_db() {
        User user = mock(User.class);
        given(user.getRole()).willReturn(Role.ROLE_USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNotificationRepository.existsByUserIdAndNotificationType(1L, NotificationType.COUPON))
                .willReturn(false);
        given(valueOperations.get("coupon_stock")).willReturn("-1");

        ResponseCouponDto result = couponService.findCoupon(1L);

        assertThat(result.isSuccess()).isFalse();
        verify(couponRepository, never()).count();
    }

    @Test
    @DisplayName("Redis 캐시 미설정(null)이면 DB 조회로 폴백한다")
    void null_stock_cache_falls_back_to_db() {
        User user = mock(User.class);
        given(user.getRole()).willReturn(Role.ROLE_USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNotificationRepository.existsByUserIdAndNotificationType(1L, NotificationType.COUPON))
                .willReturn(false);
        given(valueOperations.get("coupon_stock")).willReturn(null);
        given(couponRepository.count()).willReturn(0L);

        couponService.findCoupon(1L);

        verify(couponRepository).count();
    }

    @Test
    @DisplayName("setStock 호출 시 Redis에 지정한 수량이 저장된다")
    void set_stock_writes_to_redis() {
        couponService.setStock(100);

        verify(valueOperations).set("coupon_stock", "100");
    }

    @Test
    @DisplayName("쿠폰 발급 성공 시 Redis 잔여 수가 감소한다")
    void successful_issuance_decrements_stock() {
        User user = mock(User.class);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNotificationRepository.existsByUserIdAndNotificationType(1L, NotificationType.COUPON))
                .willReturn(false);

        Coupon coupon = new Coupon();
        given(couponRepository.findByIdWithLock(1L)).willReturn(Optional.of(coupon));

        couponService.issuanceCoupon(1L, 1L);

        verify(valueOperations).decrement("coupon_stock");
    }
}
