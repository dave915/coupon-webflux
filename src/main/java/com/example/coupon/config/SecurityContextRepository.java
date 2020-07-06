package com.example.coupon.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {
    private static final String TOKEN_PREFIX = "Bearer ";
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String authToken = getAuthToken(authHeader);
        if (Strings.isEmpty(authToken) || !jwtTokenProvider.validateToken(authToken)) {
            return Mono.empty();
        }

        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authHeader, authToken))
                .map(SecurityContextImpl::new);
    }

    private String getAuthToken(String authHeader) {
        if (Objects.nonNull(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.replace(TOKEN_PREFIX, Strings.EMPTY);
        }

        return Strings.EMPTY;
    }
}
