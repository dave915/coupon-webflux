package com.example.coupon.domain;

import java.util.List;
import java.util.stream.Collectors;

public class Coupons {
    private final List<Coupon> coupons;

    public Coupons(List<Coupon> coupons) {
        this.coupons = coupons;
    }

    public List<String> ids() {
        return coupons.stream()
                .map(Coupon::getId)
                .collect(Collectors.toList());
    }
}
