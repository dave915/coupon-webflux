package com.example.coupon.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@EqualsAndHashCode(of = {"number"}, callSuper = false)
@NoArgsConstructor
@Document(collection = "coupon_number")
public class CouponNumber extends AuditLog {
    public static final String IS_USED_COUPON_MESSAGE = "이미 사용된 쿠폰 입니다.";
    public static final String IS_UN_USED_COUPON_MESSAGE = "사용 되지 않은 쿠폰 입니다.";
    public static final String USER_NOT_MATCH_MESSAGE = "발급 받은 사용자가 아닙니다.";

    @MongoId
    private String number;
    private String couponId;
    private Long userId;
    private boolean useFlag;

    public CouponNumber(String number, String couponId) {
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
        if (useFlag) {
            throw new IllegalArgumentException(IS_USED_COUPON_MESSAGE);
        }
        this.useFlag = true;
    }

    public void cancelCoupon(long userId) {
        checkUserId(userId);
        if (!useFlag) {
            throw new IllegalArgumentException(IS_UN_USED_COUPON_MESSAGE);
        }
        this.useFlag = false;
    }

    private void checkUserId(long userId) {
        if (!isUsersCouponNumber(userId)) {
            throw new IllegalArgumentException(USER_NOT_MATCH_MESSAGE);
        }
    }
}
