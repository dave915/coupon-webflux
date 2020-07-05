package com.example.coupon.service;

import com.example.coupon.domain.*;
import com.example.coupon.repository.CouponNumberRepository;
import com.example.coupon.repository.CouponRepository;
import com.example.coupon.utils.IterableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.coupon.utils.NIOUtils.fromCallable;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {
    public static final String COUPON_NOT_FOUND_MESSAGE = "존재 하지 않는 쿠폰 입니다.";
    public static final String NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE = "발급 할 쿠폰이 없습니다.";
    public static final String COUPON_NUMBER_NOT_FOUND_MESSAGE = "존재 하지 않는 쿠폰 번호 입니다.";
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

    public Mono<CouponNumber> useCoupon(String couponNumberId, long userId) {
        return fromCallable(() -> couponNumberRepository.findById(couponNumberId)
                .orElseThrow(() -> new IllegalArgumentException(COUPON_NUMBER_NOT_FOUND_MESSAGE)))
                .flatMap(couponNumber -> validateCouponExpired(couponNumber.getCouponId())
                        .then(Mono.just(couponNumber))
                ).flatMap(couponNumber -> {
                    couponNumber.useCoupon(userId);
                    return fromCallable(() -> couponNumberRepository.save(couponNumber));
                });
    }

    private Mono<Coupon> validateCouponExpired(long couponId) {
        return fromCallable(() -> couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException(COUPON_NOT_FOUND_MESSAGE)))
                .doOnNext(coupon -> coupon.validExpired(LocalDateTime.now()));
    }

    public Mono<CouponNumber> cancelCoupon(String couponNumberId, long userId) {
        return fromCallable(() -> couponNumberRepository.findById(couponNumberId)
                .orElseThrow(() -> new IllegalArgumentException(COUPON_NUMBER_NOT_FOUND_MESSAGE)))
                .flatMap(couponNumber -> {
                    couponNumber.cancelCoupon(userId);
                    return fromCallable(() -> couponNumberRepository.save(couponNumber));
                });
    }

    public Mono<List<CouponNumber>> getUserCoupons(long userId) {
        return fromCallable(() -> couponNumberRepository.findAllByUserId(userId));
    }

    public Mono<List<CouponNumber>> getExpiredCouponNumbersBetweenDate(LocalDateTime start, LocalDateTime end) {
        return fromCallable(() -> couponRepository.findAllByExpireDateTimeBetween(start, end))
                .flatMap(this::getExpiredCouponNumbers);
    }

    private Mono<List<CouponNumber>> getExpiredCouponNumbers(List<Coupon> coupons) {
        Coupons expiredCoupons = new Coupons(coupons);
        return fromCallable(() -> couponNumberRepository.findAllByCouponIdInAndUserIdNotNull(expiredCoupons.ids()));
    }
}
