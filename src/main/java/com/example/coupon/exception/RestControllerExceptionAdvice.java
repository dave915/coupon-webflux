package com.example.coupon.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class RestControllerExceptionAdvice {
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "오류 발생";

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Mono<ErrorMessage>> defaultHandler(Exception e) {
        log.error("error >>> {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Mono.just(new ErrorMessage(INTERNAL_SERVER_ERROR_MESSAGE)));
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<Mono<ErrorMessage>> validateExceptionHandler(IllegalArgumentException e) {
        log.error("error >>> {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Mono.just(new ErrorMessage(e.getMessage())));
    }
}