package com.example.coupon.repository;

import com.example.coupon.domain.Coupon;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class CouponRepositoryTest {
    private long price;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        this.price = 1000;
    }

    @Test
    @DisplayName("Coupon insert 를 테스트 한다.")
    void insertTest() {
        Coupon coupon = new Coupon(price, LocalDateTime.now());

        long couponSize = couponRepository.count().block();
        Coupon insertedCoupon = couponRepository.save(coupon).block();

        assertThat(insertedCoupon).isNotNull();
        assertThat(couponRepository.count().block()).isGreaterThan(couponSize);

        String couponId = insertedCoupon.getId();
        Coupon dbCoupon = couponRepository.findById(new ObjectId(couponId)).block();
        assertThat(dbCoupon).isNotNull();
    }

    @Test
    @DisplayName("기간내 만료 된 쿠폰 조회")
    void findAllByExpireDateTimeBetweenTest() {
        LocalDateTime start = LocalDateTime.parse("2020-07-05T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2020-07-05T10:00:00");

        List<Coupon> coupons = Arrays.asList(
                new Coupon(0, LocalDateTime.parse("2020-07-04T23:59:59")),
                new Coupon(price, LocalDateTime.parse("2020-07-05T00:00:00")),
                new Coupon(price, LocalDateTime.parse("2020-07-05T01:00:00")),
                new Coupon(price, LocalDateTime.parse("2020-07-05T02:00:00")),
                new Coupon(price, LocalDateTime.parse("2020-07-05T10:00:00")),
                new Coupon(0, LocalDateTime.parse("2020-07-05T10:00:01"))
        );
        couponRepository.saveAll(coupons).blockLast();

        List<Coupon> expireCoupon = couponRepository.findAllByExpireDateTimeBetween(start, end)
                .collectList().block();

        assertThat(expireCoupon).hasSize(coupons.size() - 2);
        expireCoupon.forEach(coupon -> assertThat(coupon.getPrice()).isEqualTo(price));
    }
}
