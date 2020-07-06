package com.example.coupon.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String jwtToken = authentication.getCredentials().toString();
        String userId = jwtTokenProvider.getSubject(jwtToken);

        if (Objects.isNull(userId)) {
            return Mono.empty();
        }

        AbstractAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, userId, Collections.emptyList());
        authenticationToken.setDetails(null); // 추후 유저 상세정보 저장
        return Mono.just(authenticationToken);
    }
}
