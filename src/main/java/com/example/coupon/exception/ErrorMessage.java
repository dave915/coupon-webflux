package com.example.coupon.exception;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class ErrorMessage implements Serializable {
    private final String message;

    public ErrorMessage(String message) {
        this.message = message;
    }
}
