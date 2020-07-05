package com.example.coupon.repository;

import com.example.coupon.domain.CouponNumber;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CouponNumberRepository extends CrudRepository<CouponNumber, String> {
    long countByCouponIdAndUserIdNull(long couponId);

    Optional<CouponNumber> findFirstByCouponIdAndUserIdNull(long couponId);
}
