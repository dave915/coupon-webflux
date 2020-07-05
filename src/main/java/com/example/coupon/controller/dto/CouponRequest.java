package com.example.coupon.controller.dto;

import com.example.coupon.domain.Coupon;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponRequest {
    @NotNull
    private LocalDateTime expireDateTime;
    @Min(1)
    private Long price;

    public Coupon toCoupon() {
        return new Coupon(price, expireDateTime);
    }
}
