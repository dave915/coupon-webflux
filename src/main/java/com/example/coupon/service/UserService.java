package com.example.coupon.service;

import com.example.coupon.config.JwtTokenProvider;
import com.example.coupon.domain.User;
import com.example.coupon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    public static final String USER_INFO_NOT_FOUND_ERROR_MESSAGE = "사용자 정보를 찾을 수 없습니다.";
    public static final String ALREADY_EXIST_USER_ERROR_MESSAGE = "이미 존재하는 유저 입니다.";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Mono<String> signin(User user) {
        return userRepository.findFirstByUsernameAndPassword(user.getUsername(), user.getPassword())
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException(USER_INFO_NOT_FOUND_ERROR_MESSAGE)))
                .map(jwtTokenProvider::createToken);
    }

    public Mono<String> signup(User user) {
        return userRepository.countByUsername(user.getUsername())
                .flatMap(count -> insertUser(user, count));
    }

    private Mono<String> insertUser(User user, Long count) {
        if (count > 0) {
            return Mono.error(() -> new IllegalArgumentException(ALREADY_EXIST_USER_ERROR_MESSAGE));
        }

        return userRepository.save(user)
                .map(jwtTokenProvider::createToken);
    }

    @Transactional(readOnly = true)
    public Mono<User> getUserInfo(String userId) {
        return userRepository.findById(new ObjectId(userId));
    }
}
