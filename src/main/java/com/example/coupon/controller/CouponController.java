package com.example.coupon.controller;

import com.example.coupon.controller.dto.CouponRequest;
import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.CouponNumber;
import com.example.coupon.service.CouponService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Validated
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping
    public Mono<Coupon> createCoupon(@Valid @RequestBody CouponRequest couponRequest) {
        return couponService.createCoupon(couponRequest.toCoupon());
    }

    @PostMapping("/{couponId}/generate")
    public Mono<List<CouponNumber>> generateCouponNumber(@PathVariable String couponId,
                                                         @RequestParam @Min(1) int count) {
        return couponService.generateCouponNumbers(couponId, count);
    }

    @PutMapping("/{couponId}/issue")
    public Mono<CouponNumber> issueCoupon(@PathVariable String couponId, @RequestParam long userId) {
        return couponService.issueCoupon(couponId, userId);
    }

    @GetMapping("/users/{userId}")
    public Mono<List<CouponNumber>> getUserCoupons(@PathVariable long userId) {
        return couponService.getUserCoupons(userId);
    }

    @PutMapping("/{couponNumberId}")
    public Mono<CouponNumber> useCoupon(@PathVariable String couponNumberId, @RequestParam long userId) {
        return couponService.useCoupon(couponNumberId, userId);
    }

    @DeleteMapping("/{couponNumberId}")
    public Mono<CouponNumber> cancelCoupon(@PathVariable String couponNumberId, @RequestParam long userId) {
        return couponService.cancelCoupon(couponNumberId, userId);
    }

    @GetMapping("/expired/today")
    public Mono<List<CouponNumber>> getTodayExpiredCouponNumbers() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now();

        return couponService.getExpiredCouponNumbersBetweenDate(start, end);
    }

    @GetMapping("/expired/notice")
    public Mono<Void> noticeExpiredCouponNumberBetweenDate(@RequestParam LocalDate noticeDate) {
        LocalDate date = Objects.nonNull(noticeDate) ? noticeDate : LocalDate.now().plusDays(3);
        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

        return couponService.noticeExpiredCouponNumberBetweenDate(start, end);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(dataType = "__file", name = "files", required = true, paramType = "form")
    })
    @PostMapping(value = "/csv-import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Mono<Void> upload(@RequestPart("files") Flux<FilePart> filePartFlux) {
        return couponService.csvUpload(filePartFlux);
    }
}
