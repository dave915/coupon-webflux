package com.example.coupon.service;

import com.example.coupon.domain.*;
import com.example.coupon.repository.CouponNumberRepository;
import com.example.coupon.repository.CouponRepository;
import com.example.coupon.utils.FileUtils;
import com.example.coupon.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.coupon.utils.FunctionWithException.wrapper;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {
    public static final String COUPON_NOT_FOUND_MESSAGE = "존재 하지 않는 쿠폰 입니다.";
    public static final String NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE = "발급 할 쿠폰이 없습니다.";
    public static final String COUPON_NUMBER_NOT_FOUND_MESSAGE = "존재 하지 않는 쿠폰 번호 입니다.";
    public static final String CAN_NOT_EXPIRE_NOTICE_BEFORE_DATE = "이미 만료된 쿠폰은 만료 공지를 할 수 없습니다.";
    public static final int CSV_UPLOAD_BATCH_SIZE = 100;

    private final CouponRepository couponRepository;
    private final CouponNumberRepository couponNumberRepository;
//    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public Mono<Coupon> createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    public Mono<List<CouponNumber>> generateCouponNumbers(String couponId, int count) {
        return validateCouponExpired(couponId)
                .flatMap(coupon -> insertCouponNumbers(coupon.getId(), count));
    }

    private Mono<List<CouponNumber>> insertCouponNumbers(String couponId, int count) {
        CouponNumbers couponNumbers = CouponNumberGenerator.generateNumbers(couponId, count);
        return couponNumberRepository.saveAll(couponNumbers)
                .collectList();
    }

    public Mono<CouponNumber> issueCoupon(String couponId, String userId) {
        return validateCouponExpired(couponId)
                .flatMap(coupon -> couponNumberRepository.findFirstByCouponIdAndUserIdNull(coupon.getId()))
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException(NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE)))
                .flatMap(couponNumber -> issueToUser(couponNumber, userId));
    }

    private Mono<CouponNumber> issueToUser(CouponNumber couponNumber, String userId) {
        couponNumber.issue(userId);
        return couponNumberRepository.save(couponNumber);
    }

    public Mono<CouponNumber> useCoupon(String couponNumberId, String userId) {
        return couponNumberRepository.findById(couponNumberId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException(COUPON_NUMBER_NOT_FOUND_MESSAGE)))
                .flatMap(couponNumber -> validateCouponExpired(couponNumber.getCouponId())
                        .then(Mono.just(couponNumber))
                ).flatMap(couponNumber -> {
                    couponNumber.useCoupon(userId);
                    return couponNumberRepository.save(couponNumber);
                });
    }

    private Mono<Coupon> validateCouponExpired(String couponId) {
        return couponRepository.findById(new ObjectId(couponId))
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException(COUPON_NOT_FOUND_MESSAGE)))
                .doOnNext(coupon -> coupon.validExpired(LocalDateTime.now()));
    }

    public Mono<CouponNumber> cancelCoupon(String couponNumberId, String userId) {
        return couponNumberRepository.findById(couponNumberId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException(COUPON_NUMBER_NOT_FOUND_MESSAGE)))
                .flatMap(couponNumber -> {
                    couponNumber.cancelCoupon(userId);
                    return couponNumberRepository.save(couponNumber);
                });
    }

    public Mono<List<CouponNumber>> getUserCoupons(String userId) {
        return couponNumberRepository.findAllByUserId(userId)
                .collectList();
    }

    public Mono<List<CouponNumber>> getExpiredCouponNumbersBetweenDate(LocalDateTime start, LocalDateTime end) {
        return couponRepository.findAllByExpireDateTimeBetween(start, end)
                .collectList()
                .flatMap(this::getExpiredCouponNumbers);
    }

    private Mono<List<CouponNumber>> getExpiredCouponNumbers(List<Coupon> coupons) {
        Coupons expiredCoupons = new Coupons(coupons);
        return couponNumberRepository.findAllByCouponIdInAndUserIdNotNull(expiredCoupons.ids())
                .collectList();
    }

    public Mono<Void> noticeExpiredCouponNumberBetweenDate(LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today)) {
            throw new IllegalArgumentException(CAN_NOT_EXPIRE_NOTICE_BEFORE_DATE);
        }

        int distance = startDate.until(today).getDays();
        return getExpiredCouponNumbersBetweenDate(start, end)
                .doOnNext(couponNumbers -> noticeExpiredToUser(couponNumbers, distance))
                .then();
    }

    private void noticeExpiredToUser(List<CouponNumber> allUserCouponNumbers, int distance) {
        allUserCouponNumbers.stream()
                .collect(Collectors.groupingBy(CouponNumber::getUserId,
                        Collectors.collectingAndThen(Collectors.toList(), CouponNumbers::new)))
                .forEach((userId, couponNumbers) ->
                        log.info("쿠폰이 {}일 후 만료 됩니다. userId >>> {}, couponNumbers >>> {}", distance, userId, couponNumbers.distinctNumbers())
                );
    }

    public Mono<Void> csvUpload(Flux<FilePart> filePartFlux) {
        return FileUtils.readFilePartFlux(filePartFlux, CSV_UPLOAD_BATCH_SIZE, (fields, strings) -> {
            if (CollectionUtils.isEmpty(strings)) {
                return Mono.empty();
            }

            List<CouponNumber> couponNumbers = strings.stream()
                    .map(wrapper(string -> ObjectUtils.csvRowToObject(string, fields, CouponNumber.class)))
                    .collect(Collectors.toList());


            return couponNumberRepository.saveAll(couponNumbers)
                    .then();
//            return reactiveMongoTemplate.insertAll(couponNumbers) // bulk insert (중복 쿠폰 번호 있을시 Exception 발생)
//                    .then();
        });
    }
}
