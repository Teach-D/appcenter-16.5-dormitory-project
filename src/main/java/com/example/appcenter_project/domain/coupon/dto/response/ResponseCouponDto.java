package com.example.appcenter_project.domain.coupon.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseCouponDto {

    private boolean success;
    private boolean isIssued;

    public static ResponseCouponDto of(boolean success, boolean isIssued) {
        return ResponseCouponDto.builder()
                .success(success)
                .isIssued(isIssued)
                .build();
    }
}
