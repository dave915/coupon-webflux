package com.example.coupon.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CouponNumberGeneratorTest {
    @Test
    @DisplayName("n 개의 CouponNumber 를 생성 한다.")
    void generateNumbersTest() {
        long couponId = 1;
        int count = 3;

        CouponNumbers couponNumbers = CouponNumberGenerator.generateNumbers(couponId, count);

        assertThat(couponNumbers.size()).isEqualTo(count);
        assertThat(couponNumbers.distinctNumbers()).hasSize(count);
    }

    @Test
    @DisplayName("매번 다른 number를 가지는 CouponNumber 를 생성 한다.")
    void generateNumberTest() {
       long couponId = 1;

       CouponNumber first = CouponNumberGenerator.generateNumber(couponId);
       CouponNumber second = CouponNumberGenerator.generateNumber(couponId);

       assertThat(first).isNotEqualTo(second);
    }
}
