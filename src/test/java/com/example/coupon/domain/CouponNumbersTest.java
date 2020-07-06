package com.example.coupon.domain;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CouponNumbersTest {
    @Test
    @DisplayName("쿠폰 size를 리턴한다")
    void sizeTest() {
        List<CouponNumber> numbers = Lists.list(new CouponNumber(), new CouponNumber());
        CouponNumbers couponNumbers = new CouponNumbers(numbers);

        assertThat(couponNumbers.size()).isEqualTo(numbers.size());
    }

    @Test
    @DisplayName("중복된 쿠폰 번호를 제외하고 리턴하는지 테스트한다")
    void distinctNumbersTest() {
        String couponId = "1";
        int expectedSize = 1;
        List<CouponNumber> numbers = Lists.list(new CouponNumber("1", couponId), new CouponNumber("1", couponId));
        CouponNumbers couponNumbers = new CouponNumbers(numbers);

        assertThat(couponNumbers.distinctNumbers()).hasSize(expectedSize);
    }
}
