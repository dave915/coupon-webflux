package com.example.coupon.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@EqualsAndHashCode(of = {"number"}, callSuper = false)
@NoArgsConstructor
public class CouponNumber extends AuditLog {
    public static final String IS_USED_COUPON_MESSAGE = "이미 사용된 쿠폰 입니다.";
    public static final String USER_NOT_MATCH_MESSAGE = "발급받은 사용자가 아닙니다.";

    @Id
    private long id;
    private String number;
    private Long couponId;
    private Long userId;
    private boolean use;

    public CouponNumber(String number, long couponId) {
        this.number = number;
        this.couponId = couponId;
    }

    public void issue(long userId) {
        this.userId = userId;
    }

    public boolean isUsersCouponNumber(long userId) {
        return this.userId == userId;
    }

    public void useCoupon(long userId) {
        checkUserId(userId);
        if (use) {
            throw new IllegalArgumentException(IS_USED_COUPON_MESSAGE);
        }
        this.use = true;
    }

    private void checkUserId(long userId) {
        if (!isUsersCouponNumber(userId)) {
            throw new IllegalArgumentException(USER_NOT_MATCH_MESSAGE);
        }
    }
}
