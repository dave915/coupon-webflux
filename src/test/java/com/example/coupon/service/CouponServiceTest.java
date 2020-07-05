package com.example.coupon.service;

import com.example.coupon.domain.Coupon;
import com.example.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepository);
    }

    @Test
    @DisplayName("쿠폰을 생성 한다.")
    void createCouponTest() {
        long expectedId = 1;
        LocalDateTime expireDateTime = LocalDateTime.now().plusDays(3);
        long price = 1000;
        Coupon coupon = new Coupon(price, expireDateTime);
        given(couponRepository.save(any())).willReturn(new Coupon(expectedId, price, expireDateTime));

        Coupon createdCoupon = couponService.createCoupon(coupon)
                .block();

        assertThat(createdCoupon.getId()).isEqualTo(expectedId);
        assertThat(createdCoupon.getPrice()).isEqualTo(price);
        assertThat(createdCoupon.getExpireDateTime()).isEqualTo(expireDateTime);
    }
}
