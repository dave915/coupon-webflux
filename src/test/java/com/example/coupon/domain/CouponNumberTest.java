package com.example.coupon.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CouponNumberTest {
    private long userId;
    private CouponNumber couponNumber;

    @BeforeEach
    void setUp() {
        this.userId = 1;
        this.couponNumber = new CouponNumber("11", 1);
    }

    @Test
    @DisplayName("쿠폰번호를 사용한다")
    void useCouponTest() {
        couponNumber.issue(userId);

        couponNumber.useCoupon(userId);

        assertThat(couponNumber.isUse()).isTrue();
    }

    @Test
    @DisplayName("쿠폰번호 사용시 발급받은 유저가 아닐 경우 오류가 발생한다")
    void useCoupon_notMatchUserUseTest() {
        couponNumber.issue(userId);

        assertThatThrownBy(() -> couponNumber.useCoupon(4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.USER_NOT_MATCH_MESSAGE);
    }

    @Test
    @DisplayName("쿠폰번호 사용시 이미 사용된 쿠폰 일 경우 오류가 발생한다")
    void useCoupon_usedCouponTest() {
        couponNumber.issue(userId);

        couponNumber.useCoupon(userId);
        assertThatThrownBy(() -> couponNumber.useCoupon(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.IS_USED_COUPON_MESSAGE);
    }
}
