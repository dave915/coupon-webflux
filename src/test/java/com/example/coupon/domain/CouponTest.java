package com.example.coupon.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CouponTest {
    @Test
    @DisplayName("쿠폰이 만료 되었는지 확인한다")
    void validExpiredTest() {
        LocalDateTime now = LocalDateTime.parse("2020-07-05T00:00:01");
        LocalDateTime expireTime = LocalDateTime.parse("2020-07-05T00:00:00");
        Coupon coupon = new Coupon(1000, expireTime);

        assertThatThrownBy(() -> coupon.validExpired(now))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
