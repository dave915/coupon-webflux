package com.example.coupon.repository;

import com.example.coupon.domain.Coupon;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends CrudRepository<Coupon, Long> {
    List<Coupon> findAllByExpireDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
