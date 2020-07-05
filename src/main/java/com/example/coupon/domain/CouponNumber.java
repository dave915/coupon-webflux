package com.example.coupon.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@EqualsAndHashCode(of = {"number"}, callSuper = false)
@NoArgsConstructor
public class CouponNumber extends AuditLog {
    @Id
    private long id;
    private String number;
    private long couponId;
    private String userId;
    private boolean use;

    public CouponNumber(String number, long couponId) {
        this.number = number;
        this.couponId = couponId;
    }
}
