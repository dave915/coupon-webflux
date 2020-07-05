package com.example.coupon.domain;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CouponNumberGenerator {
    private static final int GENERATE_START_INDEX = 0;

    public static CouponNumbers generateNumbers(long couponId, int count) {
        return IntStream.range(GENERATE_START_INDEX, count)
                .mapToObj(i -> generateNumber(couponId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), CouponNumbers::new));
    }

    public static CouponNumber generateNumber(long couponId) {
        String number = UUID.randomUUID().toString();
        return new CouponNumber(number, couponId);
    }
}
