package com.example.coupon.service;

import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.CouponNumber;
import com.example.coupon.domain.CouponNumberGenerator;
import com.example.coupon.domain.CouponNumbers;
import com.example.coupon.repository.CouponNumberRepository;
import com.example.coupon.repository.CouponRepository;
import com.example.coupon.utils.IterableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {
    public static final String COUPON_NOT_FOUND_MESSAGE = "존재 하지 않는 쿠폰 입니다.";
    private final CouponRepository couponRepository;
    private final CouponNumberRepository couponNumberRepository;

    public Mono<Coupon> createCoupon(Coupon coupon) {
        return Mono.fromCallable(() -> couponRepository.save(coupon))
                .subscribeOn(Schedulers.elastic());
    }

    public Mono<List<CouponNumber>> generateCouponNumbers(long couponId, int count) {
        return validateCouponExpired(couponId)
                .then(insertCouponNumbers(couponId, count));
    }

    private Mono<List<CouponNumber>> insertCouponNumbers(long couponId, int count) {
        CouponNumbers couponNumbers = CouponNumberGenerator.generateNumbers(couponId, count);
        return Mono.fromCallable(() -> IterableUtils.toList(couponNumberRepository.saveAll(couponNumbers)))
                .subscribeOn(Schedulers.elastic());
    }

    private Mono<Coupon> validateCouponExpired(long couponId) {
        return Mono.fromCallable(() -> couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException(COUPON_NOT_FOUND_MESSAGE)))
                .doOnNext(Coupon::validExpired);
    }
}
