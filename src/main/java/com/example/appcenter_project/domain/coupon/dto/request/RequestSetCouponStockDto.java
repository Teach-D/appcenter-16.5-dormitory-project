package com.example.appcenter_project.domain.coupon.dto.request;

import jakarta.validation.constraints.Min;

public record RequestSetCouponStockDto(
        @Min(value = 0, message = "쿠폰 수량은 0 이상이어야 합니다.")
        int count
) {
}
