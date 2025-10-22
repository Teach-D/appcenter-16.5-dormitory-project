package com.example.appcenter_project.domain.coupon.service;

import com.example.appcenter_project.domain.coupon.dto.response.ResponseCouponDto;
import com.example.appcenter_project.domain.coupon.entity.Coupon;
import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.shared.enums.ApiType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.coupon.repository.CouponRepository;
import com.example.appcenter_project.domain.notification.repository.NotificationRepository;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private static final String REDIS_KEY = "coupon_date_time";

    private final CouponRepository couponRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public ResponseCouponDto findCoupon(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != Role.ROLE_USER) {
            log.info("관리자는 쿠폰을 받을 수 없습니다", userId);
            return ResponseCouponDto.builder()
                    .isIssued(false)
                    .success(false)
                    .build();
        }

        log.info("쿠폰 조회 요청 - userId: {}", userId);

        // 1. 가장 먼저 중복 발급 체크 (가장 빠른 반환)
        boolean hasCouponNotification = userNotificationRepository
                .existsByUserIdAndNotificationType(userId, NotificationType.COUPON);
        
        if (hasCouponNotification) {
            log.info("사용자 {}는 이미 쿠폰을 발급받았습니다.", userId);
            return ResponseCouponDto.builder()
                    .isIssued(true)
                    .success(true)
                    .build();
        }

        // 2. 쿠폰 존재 여부 확인
        if (couponRepository.count() == 0) {
            log.warn("발급 가능한 쿠폰이 없습니다.");
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        // 3. Redis에서 쿠폰 오픈 시간 확인
        String couponOpenTimeStr = redisTemplate.opsForValue().get(REDIS_KEY);
        if (couponOpenTimeStr == null) {
            log.warn("쿠폰 오픈 시간이 설정되지 않았습니다.");
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        // 4. 시간 검증 (오픈 시간 이전이면 반환)
        LocalTime couponOpenTime = LocalTime.parse(couponOpenTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        if (currentTime.isBefore(couponOpenTime)) {
            log.info("아직 쿠폰 오픈 시간이 아닙니다. 오픈 시간: {}, 현재 시간: {}", 
                    couponOpenTime, currentTime);
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        // 5. 요일에 따른 쿠폰 ID 결정
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        Long couponId = getCouponIdByDayOfWeek(dayOfWeek);

        if (couponId == null) {
            log.warn("해당 요일({})에 발급 가능한 쿠폰이 없습니다.", dayOfWeek);
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        // 6. 쿠폰 발급 (별도 트랜잭션)
        return issuanceCoupon(userId, couponId);
    }

    @Transactional
    public ResponseCouponDto issuanceCoupon(Long userId, Long couponId) {
        try {
            // 1. 쿠폰 Lock 획득
            Coupon coupon = couponRepository.findByIdWithLock(couponId)
                    .orElse(null);

            // 2. Lock 내부에서 한번 더 중복 체크 (Double Check - Race Condition 완전 방지)
            boolean hasCouponNotification = userNotificationRepository
                    .existsByUserIdAndNotificationType(userId, NotificationType.COUPON);
            
            if (hasCouponNotification) {
                log.info("사용자 {}는 이미 쿠폰을 발급받았습니다. (Lock 내부 체크)", userId);
                return ResponseCouponDto.builder()
                        .isIssued(true)
                        .success(true)
                        .build();
            }

            if (coupon == null) {
                log.info("쿠폰이 이미 소진되었습니다 - userId: {}, couponId: {}", userId, couponId);
                return ResponseCouponDto.builder()
                        .success(false)
                        .build();
            }

            // 3. 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 4. 알림 생성 및 저장
            Notification notification = Notification.builder()
                    .title("쿠폰에 당첨되셨습니다!")
                    .body("에브리타임에 제목에 유니돔을 포함해서 자유게시판에 게시글을 올려주세요!")
                    .notificationType(NotificationType.COUPON)
                    .apiType(ApiType.COUPON)
                    .build();

            notificationRepository.save(notification);

            UserNotification userNotification = UserNotification.of(user, notification);
            userNotificationRepository.save(userNotification);

            // 5. 쿠폰 삭제 (발급 완료)
            couponRepository.delete(coupon);

            log.info("쿠폰 발급 성공 - userId: {}, couponId: {}", userId, coupon.getId());

            return ResponseCouponDto.builder()
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("쿠폰 발급 실패 - userId: {}, couponId: {}, error: {}", 
                    userId, couponId, e.getMessage(), e);
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }
    }

    private Long getCouponIdByDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> 1L;
            case TUESDAY -> 2L;
            case WEDNESDAY -> 3L;
            case THURSDAY -> 4L;
            case FRIDAY -> 5L;
            default -> null; // 주말 (토, 일)
        };
    }
}
