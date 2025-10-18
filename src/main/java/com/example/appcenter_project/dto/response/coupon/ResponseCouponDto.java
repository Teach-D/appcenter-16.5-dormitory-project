package com.example.appcenter_project.dto.response.coupon;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ResponseCouponDto {

    private boolean success;
    private boolean isIssued;
}
