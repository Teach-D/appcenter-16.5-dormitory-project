package com.example.appcenter_project.domain.coupon.controller;

import com.example.appcenter_project.domain.coupon.dto.request.RequestSetCouponStockDto;
import com.example.appcenter_project.domain.coupon.dto.response.ResponseCouponDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<ResponseCouponDto> findCoupon(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(couponService.findCoupon(userDetails.getId()));
    }

    @PostMapping("/admin/stock")
    public ResponseEntity<Void> setStock(@Valid @RequestBody RequestSetCouponStockDto request) {
        couponService.setStock(request.count());
        return ResponseEntity.ok().build();
    }
}
