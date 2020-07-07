package com.example.coupon.domain;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CouponNumbers implements Iterable<CouponNumber> {
    private final List<CouponNumber> couponNumbers;

    public CouponNumbers(List<CouponNumber> couponNumbers) {
        this.couponNumbers = couponNumbers;
    }

    public int size() {
        return couponNumbers.size();
    }

    public List<String> distinctNumbers() {
        return couponNumbers.stream()
                .map(CouponNumber::getNumber)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<CouponNumber> iterator() {
        return couponNumbers.iterator();
    }
}
