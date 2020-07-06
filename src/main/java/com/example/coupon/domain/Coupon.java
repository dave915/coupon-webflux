package com.example.coupon.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "coupon")
public class Coupon extends AuditLog {
    public static final String COUPON_EXPIRED_MESSAGE = "유효기간이 지난 쿠폰입니다.";

    @MongoId
    private ObjectId id;
    private long price;
    private LocalDateTime expireDateTime;

    public Coupon(long price, LocalDateTime expiredTime) {
        this.price = price;
        this.expireDateTime = expiredTime;
    }

    public void validExpired(LocalDateTime now) {
        if (now.isAfter(expireDateTime)) {
            throw new IllegalArgumentException(COUPON_EXPIRED_MESSAGE);
        }
    }

    public String getId() {
        return id.toString();
    }
}