package com.example.coupon.domain;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CouponNumbers implements Iterable<CouponNumber>{
    private final List<CouponNumber> numbers;

    public CouponNumbers(List<CouponNumber> couponNumbers) {
        this.numbers = couponNumbers;
    }

    public int size() {
        return numbers.size();
    }

    public List<String> distinctNumbers() {
        return numbers.stream()
                .map(CouponNumber::getNumber)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<CouponNumber> iterator() {
        return numbers.iterator();
    }
}
