package com.example.coupon.repository;

import com.example.coupon.domain.CouponNumber;
import org.springframework.data.repository.CrudRepository;

public interface CouponNumberRepository extends CrudRepository<CouponNumber, String> {
}
