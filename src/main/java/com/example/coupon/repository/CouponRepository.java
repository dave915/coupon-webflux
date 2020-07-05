package com.example.coupon.repository;

import com.example.coupon.domain.Coupon;
import org.springframework.data.repository.CrudRepository;

public interface CouponRepository extends CrudRepository<Coupon, String> {
}
