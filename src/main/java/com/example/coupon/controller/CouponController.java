package com.example.coupon.controller;

import com.example.coupon.controller.dto.CouponRequest;
import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.CouponNumber;
import com.example.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping
    public Mono<Coupon> createCoupon(@Valid @RequestBody CouponRequest couponRequest) {
        return couponService.createCoupon(couponRequest.toCoupon());
    }

    @PostMapping("/{couponId}/generate")
    public Mono<List<CouponNumber>> generateCouponNumber(@PathVariable long couponId,
                                                         @RequestParam @Min(1) int count) {
        return couponService.generateCouponNumbers(couponId, count);
    }

    @PutMapping("/{couponId}/issue")
    public Mono<CouponNumber> issueCoupon(@PathVariable long couponId, @RequestParam long userId) {
        return couponService.issueCoupon(couponId, userId);
    }

    @GetMapping("/users/{userId}")
    public Mono<List<CouponNumber>> getUserCoupons(@PathVariable long userId) {
        return couponService.getUserCoupons(userId);
    }
}
