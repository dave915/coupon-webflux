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

import java.util.List;
import java.util.Optional;

import static com.example.coupon.utils.NIOUtils.fromCallable;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {
    public static final String COUPON_NOT_FOUND_MESSAGE = "존재 하지 않는 쿠폰 입니다.";
    public static final String NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE = "발급 할 쿠폰이 없습니다.";
    private final CouponRepository couponRepository;
    private final CouponNumberRepository couponNumberRepository;

    public Mono<Coupon> createCoupon(Coupon coupon) {
        return fromCallable(() -> couponRepository.save(coupon));
    }

    public Mono<List<CouponNumber>> generateCouponNumbers(long couponId, int count) {
        return validateCouponExpired(couponId)
                .then(insertCouponNumbers(couponId, count));
    }

    private Mono<List<CouponNumber>> insertCouponNumbers(long couponId, int count) {
        CouponNumbers couponNumbers = CouponNumberGenerator.generateNumbers(couponId, count);
        return fromCallable(() -> IterableUtils.toList(couponNumberRepository.saveAll(couponNumbers)));
    }

    public Mono<CouponNumber> issueCoupon(long couponId, long userId) {
        return validateCouponExpired(couponId)
                .then(fromCallable(() -> couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId)))
                .flatMap(couponNumber -> issueToUser(couponNumber, userId));
    }

    private Mono<CouponNumber> issueToUser(Optional<CouponNumber> couponNumber, long userId) {
        return couponNumber.map(it -> {
            it.issue(userId);
            return fromCallable(() -> couponNumberRepository.save(it));
        }).orElseThrow(() -> new IllegalArgumentException(NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE));
    }

    private Mono<Coupon> validateCouponExpired(long couponId) {
        return fromCallable(() -> couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException(COUPON_NOT_FOUND_MESSAGE)))
                .doOnNext(Coupon::validExpired);
    }

    public Mono<List<CouponNumber>> getUserCoupons(long userId) {
        return fromCallable(() -> couponNumberRepository.findAllByUserId(userId));
    }
}
