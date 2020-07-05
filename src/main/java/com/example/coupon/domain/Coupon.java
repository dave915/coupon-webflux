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
    @Id
    private Long id;
    private long price;
    private LocalDateTime expireDateTime;

    public Coupon(long price, LocalDateTime expiredTime) {
        this.price = price;
        this.expireDateTime = expiredTime;
    }
}