package com.example.coupon.utils;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

public class NIOUtils {
    public static <T> Mono<T> fromCallable(Callable<T> supplier) {
        return Mono.fromCallable(supplier)
                .subscribeOn(Schedulers.elastic());
    }
}
