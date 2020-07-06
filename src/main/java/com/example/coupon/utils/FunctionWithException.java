package com.example.coupon.utils;

import java.util.function.Function;

@FunctionalInterface
public interface FunctionWithException<T, R, E extends Exception> {
    R apply(T t) throws E;

    static <T, R, E extends Exception> Function<T, R> wrapper(FunctionWithException<T, R, E> function) {
        return arg -> {
            try {
                return function.apply(arg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
