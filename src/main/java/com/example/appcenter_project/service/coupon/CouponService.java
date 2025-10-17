package com.example.appcenter_project.service.coupon;

import com.example.appcenter_project.dto.response.coupon.ResponseCouponDto;
import com.example.appcenter_project.entity.coupon.Coupon;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.ApiType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.coupon.CouponRepository;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private static final String REDIS_KEY = "coupon_date_time";

    private final CouponRepository couponRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public ResponseCouponDto findCoupon(Long userId) {
        if (couponRepository.findAll().isEmpty()) {
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        String couponOpenTimeStr = redisTemplate.opsForValue().get(REDIS_KEY);

        if (couponOpenTimeStr == null) {
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        LocalTime couponOpenTime = LocalTime.parse(couponOpenTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();

        // 현재 시간이 쿠폰 오픈 시간과 같거나 이후인지 확인
        if (currentTime.getHour() != couponOpenTime.getHour() || currentTime.getMinute() != couponOpenTime.getMinute()) {
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }


        // 요일에 따른 쿠폰 ID 결정
        Long couponId = getCouponIdByDayOfWeek(dayOfWeek);

        if (couponId == null) {
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        return issuanceCoupon(userId, couponId);
    }

    private synchronized ResponseCouponDto issuanceCoupon(Long userId, Long couponId) {
        // Pessimistic Lock으로 쿠폰 조회
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElse(null);

        if (coupon == null) {
//            throw new RuntimeException();
            return ResponseCouponDto.builder()
                    .success(false)
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 알림 생성 및 저장
        Notification notification = Notification.builder()
                .title("배민 쿠폰번호는 " + coupon.getCode() + "입니다!")
                .body(coupon.getStartDate() + "~" + coupon.getEndDate())
                .notificationType(NotificationType.COUPON)
                .apiType(ApiType.COUPON)
                .build();

        notificationRepository.save(notification);

        UserNotification userNotification = UserNotification.of(user, notification);
        userNotificationRepository.save(userNotification);

        // 쿠폰 삭제
        couponRepository.delete(coupon);

        return ResponseCouponDto.builder()
                .success(true)
                .code(coupon.getCode())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .build();
    }

    private Long getCouponIdByDayOfWeek(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return 1L;
            case TUESDAY:
                return 2L;
            case WEDNESDAY:
                return 3L;
            case THURSDAY:
                return 4L;
            case FRIDAY:
                return 5L;
            default:
                return null; // 주말
        }
    }
}
