package com.example.coupon.repository;


import com.example.coupon.domain.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, ObjectId> {
    Mono<User> findFirstByUsernameAndPassword(String username, String password);

    Mono<Long> countByUsername(String username);
}
