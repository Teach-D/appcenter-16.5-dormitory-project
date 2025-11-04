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

    // ========== Public Methods ========== //

    @Transactional
    public ResponseCouponDto findCoupon(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        if (isNotUserRole(user)) {
            log.info("관리자는 쿠폰을 받을 수 없습니다", userId);
            return ResponseCouponDto.of(false, false);
        }

        if (isAlreadyHaveCoupon(userId)) {
            log.info("사용자 {}는 이미 쿠폰을 발급받았습니다.", userId);
            return ResponseCouponDto.of(true, true);
        }

        if (isNotExistsCoupon()) {
            log.info("쿠폰이 존재하지 않습니다.");
            return ResponseCouponDto.of(false, false);
        }

        String couponOpenTimeString = redisTemplate.opsForValue().get(REDIS_KEY);
        if (couponOpenTimeString == null) {
            log.info("쿠폰 오픈 시간이 설정되지 않았습니다.");
            return ResponseCouponDto.of(false, false);
        }

        LocalTime couponOpenTime = LocalTime.parse(couponOpenTimeString, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime currentTime = LocalDateTime.now().toLocalTime();
        if (isCurrentTimeIsBeforeCouponOpenTime(currentTime, couponOpenTime)) {
            log.info("아직 쿠폰 오픈 시간이 아닙니다. 오픈 시간: {}, 현재 시간: {}",
                    couponOpenTime, currentTime);
            return ResponseCouponDto.of(false, false);
        }

        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        Long couponId = getCouponIdByDayOfWeek(dayOfWeek);
        if (couponId == null) {
            log.warn("해당 요일({})에 발급 가능한 쿠폰이 없습니다.", dayOfWeek);
            return ResponseCouponDto.of(false, false);
        }

        return issuanceCoupon(userId, couponId);
    }

    @Transactional
    public ResponseCouponDto issuanceCoupon(Long userId, Long couponId) {
        try {
            Coupon coupon = couponRepository.findByIdWithLock(couponId)
                    .orElse(null);

            if (isAlreadyHaveCoupon(userId)) {
                log.info("사용자 {}는 이미 쿠폰을 발급받았습니다. (Lock 내부 체크)", userId);
                return ResponseCouponDto.of(true, true);
            }

            if (coupon == null) {
                log.info("쿠폰이 이미 소진되었습니다 - userId: {}, couponId: {}", userId, couponId);
                return ResponseCouponDto.of(false, false);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            Notification notification = createNotification();
            createUserNotification(user, notification);

            couponRepository.delete(coupon);

            log.info("쿠폰 발급 성공 - userId: {}, couponId: {}", userId, coupon.getId());
            return ResponseCouponDto.of(true, false);

        } catch (Exception e) {
            log.error("쿠폰 발급 실패 - userId: {}, couponId: {}, error: {}",
                    userId, couponId, e.getMessage(), e);
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }
    }

    // ========== Private Methods ========== //

    private static boolean isNotUserRole(User user) {
        return user.getRole() != Role.ROLE_USER;
    }

    private boolean isAlreadyHaveCoupon(Long userId) {
        return userNotificationRepository
                .existsByUserIdAndNotificationType(userId, NotificationType.COUPON);
    }

    private boolean isNotExistsCoupon() {
        return couponRepository.count() == 0;
    }

    private static boolean isCurrentTimeIsBeforeCouponOpenTime(LocalTime currentTime, LocalTime couponOpenTime) {
        return currentTime.isBefore(couponOpenTime);
    }


    private Notification createNotification() {
        Notification notification = Notification.builder()
                .title("쿠폰에 당첨되셨습니다!")
                .body("에브리타임에 제목에 유니돔을 포함해서 자유게시판에 게시글을 올려주세요!")
                .notificationType(NotificationType.COUPON)
                .apiType(ApiType.COUPON)
                .build();

        notificationRepository.save(notification);
        return notification;
    }

    private void createUserNotification(User user, Notification notification) {
        UserNotification userNotification = UserNotification.of(user, notification);
        userNotificationRepository.save(userNotification);
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