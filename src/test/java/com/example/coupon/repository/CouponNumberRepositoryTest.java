package com.example.coupon.repository;

import com.example.coupon.domain.CouponNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@ExtendWith(SpringExtension.class)
public class CouponNumberRepositoryTest {
    @Autowired
    private CouponNumberRepository couponNumberRepository;

    @Test
    @DisplayName("Batch insert를 테스트 한다.")
    void saveAllTest() {
        List<CouponNumber> couponNumbers = Arrays.asList(
                new CouponNumber("11", 1),
                new CouponNumber("22", 1),
                new CouponNumber("33", 1)
        );

        Iterable<CouponNumber> insertedCouponNumbers = couponNumberRepository.saveAll(couponNumbers);

        assertThat(insertedCouponNumbers).hasSize(couponNumbers.size());
    }
}
