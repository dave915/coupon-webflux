package com.example.coupon.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
    public static final String COUPON_EXPIRED_MESSAGE = "유효기간이 지난 쿠폰입니다.";

    @Id
    private Long id;
    private long price;
    private LocalDateTime expireDateTime;

    public Coupon(long price, LocalDateTime expiredTime) {
        this.price = price;
        this.expireDateTime = expiredTime;
    }

    public void validExpired() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expireDateTime)) {
            throw new IllegalArgumentException(COUPON_EXPIRED_MESSAGE);
        }
    }
}