package com.example.coupon.repository;

import com.example.coupon.domain.Coupon;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface CouponRepository extends ReactiveMongoRepository<Coupon, ObjectId> {
    @Query("{ expireDateTime : {'$gte' : ?0, '$lte' : ?1} }")
    Flux<Coupon> findAllByExpireDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
