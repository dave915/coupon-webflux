package com.example.coupon.config;

import com.example.coupon.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.token.secret-key}")
    private String secretKey;
    @Value("${jwt.token.expire-time}")
    private long tokenExpireTime;
    private String encodedSecretKey;

    @PostConstruct
    protected void init() {
        encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(user.getId()));
        claims.put("name", user.getUsername());
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + tokenExpireTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS256, encodedSecretKey)
                .compact();
    }

    public String getSubject(String token) {
        return getClaims(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = getClaims(token);
            return !claims.getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            log.error("jwt 토큰 유효성 체크 실패");
            return false;
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(encodedSecretKey)
                .parseClaimsJws(token);
    }
}