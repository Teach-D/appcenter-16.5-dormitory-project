package com.example.appcenter_project.controller.coupon;

import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.dto.response.coupon.ResponseCouponDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
