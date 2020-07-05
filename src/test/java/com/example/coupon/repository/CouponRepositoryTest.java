package com.example.coupon.repository;

import com.example.coupon.domain.Coupon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@ExtendWith(SpringExtension.class)
public class CouponRepositoryTest {
    @Autowired
    private CouponRepository couponRepository;

    @Test
    @DisplayName("Coupon insert 를 테스트 한다.")
    void insertTest() {
        Coupon coupon = new Coupon(1000, LocalDateTime.now());

        long couponSize = couponRepository.count();
        Coupon insertedCoupon = couponRepository.save(coupon);

        assertThat(couponRepository.count()).isGreaterThan(couponSize);
        assertThat(insertedCoupon.getId()).isNotNull();
    }
}
