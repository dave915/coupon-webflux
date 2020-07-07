package com.example.coupon.domain;

import java.util.List;
import java.util.stream.Collectors;

public class Coupons {
    private final List<Coupon> couponList;

    public Coupons(List<Coupon> coupons) {
        this.couponList = coupons;
    }

    public List<String> ids() {
        return couponList.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
    }
}
