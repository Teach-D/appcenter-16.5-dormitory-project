package com.example.appcenter_project.domain.coupon.controller;

import com.example.appcenter_project.domain.coupon.dto.response.ResponseCouponDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ResponseCouponDto> findCoupon(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(couponService.findCoupon(userDetails.getId()));
    }
}
