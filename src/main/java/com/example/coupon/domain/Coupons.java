package com.example.coupon.domain;

import java.util.List;
import java.util.stream.Collectors;

public class Coupons {
    private final List<Coupon> coupons;

    public Coupons(List<Coupon> coupons) {
        this.coupons = coupons;
    }

    public List<Long> ids() {
        return coupons.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
    }
}
