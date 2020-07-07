package com.example.coupon.domain;

import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class CouponNumberGenerator {
    private final int GENERATE_START_INDEX = 0;

    public CouponNumbers generateNumbers(String couponId, int count) {
        return IntStream.range(GENERATE_START_INDEX, count)
                .mapToObj(i -> generateNumber(couponId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), CouponNumbers::new));
    }

    public CouponNumber generateNumber(String couponId) {
        String number = UUID.randomUUID().toString();
        return new CouponNumber(number, couponId);
    }
}
