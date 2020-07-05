package com.example.coupon.service;

import com.example.coupon.domain.Coupon;
import com.example.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public Mono<Coupon> createCoupon(Coupon coupon) {
        return Mono.fromCallable(() -> couponRepository.save(coupon))
                .subscribeOn(Schedulers.elastic());
    }
}
