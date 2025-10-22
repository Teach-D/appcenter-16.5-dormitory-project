package com.example.appcenter_project.domain.coupon.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseCouponDto {

    private boolean success;
    private boolean isIssued;
}
