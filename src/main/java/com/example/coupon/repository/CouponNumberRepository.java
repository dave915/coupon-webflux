package com.example.coupon.repository;

import com.example.coupon.domain.CouponNumber;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CouponNumberRepository extends ReactiveMongoRepository<CouponNumber, String> {
    Mono<CouponNumber> findFirstByCouponIdAndUserIdNull(String couponId);

    Flux<CouponNumber> findAllByUserId(String userId);

    Flux<CouponNumber> findAllByCouponIdInAndUserIdNotNull(List<String> couponIds);
}
