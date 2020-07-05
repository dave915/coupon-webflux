package com.example.coupon.repository;

import com.example.coupon.domain.CouponNumber;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CouponNumberRepository extends CrudRepository<CouponNumber, String> {
    Optional<CouponNumber> findFirstByCouponIdAndUserIdNull(long couponId);

    List<CouponNumber> findAllByUserId(long userId);
}
