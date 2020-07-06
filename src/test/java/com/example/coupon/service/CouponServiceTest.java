package com.example.coupon.service;

import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.CouponNumber;
import com.example.coupon.domain.CouponNumbers;
import com.example.coupon.repository.CouponNumberRepository;
import com.example.coupon.repository.CouponRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {
    private CouponService couponService;
    private String userId;
    private String anotherUserId;
    private long price;
    private ObjectId couponId;
    private String number;
    private Coupon plusDayMockCoupon;
    private Coupon minusDayMockCoupon;
    private CouponNumber mockCouponNumber;
    private CouponNumber mockUsedCouponNumber;

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponNumberRepository couponNumberRepository;

    @BeforeEach
    void setUp() {
        this.couponService = new CouponService(couponRepository, couponNumberRepository);
        this.userId = "5f032ca9959fb7509d16d92b";
        this.anotherUserId = "4f032ca9959fb7509d16d92a";
        this.price = 1000;
        this.couponId = new ObjectId("5f032ca9959fb7509d16d92a");
        this.number = "11";
        this.plusDayMockCoupon = new Coupon(couponId, 1000, LocalDateTime.now().plusDays(3));
        this.minusDayMockCoupon = new Coupon(couponId, 1000, LocalDateTime.now().minusDays(3));
        this.mockCouponNumber = new CouponNumber(number, couponId.toString());
        this.mockUsedCouponNumber = new CouponNumber(number, couponId.toString());
        mockUsedCouponNumber.issue(userId);
        mockUsedCouponNumber.useCoupon(userId);
    }

    @Test
    @DisplayName("쿠폰을 생성 한다")
    void createCouponTest() {
        String expectedId = "5f032ca9959fb7509d16d92a";
        LocalDateTime expireDateTime = LocalDateTime.now().plusDays(3);
        Coupon coupon = new Coupon(price, expireDateTime);
        given(couponRepository.save(any())).willReturn(Mono.just(new Coupon(new ObjectId("5f032ca9959fb7509d16d92a"), price, expireDateTime)));

        Coupon createdCoupon = couponService.createCoupon(coupon)
                .block();

        assertThat(createdCoupon.getId()).isEqualTo(expectedId);
        assertThat(createdCoupon.getPrice()).isEqualTo(price);
        assertThat(createdCoupon.getExpireDateTime()).isEqualTo(expireDateTime);
    }

    @Test
    @DisplayName("쿠폰번호를 n개 생성 한다")
    void generateCouponNumberTest() {
        int count = 2;
        List<CouponNumber> mockCouponNumbers = Arrays.asList(new CouponNumber("11", couponId.toString()), new CouponNumber("22", couponId.toString()));
        given(couponRepository.findById(couponId)).willReturn(Mono.just(plusDayMockCoupon));
        given(couponNumberRepository.saveAll(any(Iterable.class))).willReturn(Flux.fromStream(mockCouponNumbers.stream()));

        List<CouponNumber> generatedCouponNumbers = couponService.generateCouponNumbers(couponId.toString(), count)
                .block();
        CouponNumbers couponNumbers = new CouponNumbers(generatedCouponNumbers);

        assertThat(couponNumbers.size()).isEqualTo(count);
        assertThat(couponNumbers.distinctNumbers()).hasSize(count);
    }

    @Test
    @DisplayName("쿠폰 번호 생성 시 만료 된 쿠폰일 경우 오류가 발생 한다")
    void generateCouponNumber_expiredCouponTest() {
        int count = 2;
        given(couponRepository.findById(any(ObjectId.class))).willReturn(Mono.just(minusDayMockCoupon));

        assertThatThrownBy(() -> couponService.generateCouponNumbers(couponId.toString(), count).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("쿠폰 번호 생성 시 쿠폰 아이디를 찾을 수 없는 경우 오류가 발생 한다")
    void generateCouponNumber_notFoundCouponTest() {
        int count = 2;
        given(couponRepository.findById(any(ObjectId.class))).willReturn(Mono.empty());

        assertThatThrownBy(() -> couponService.generateCouponNumbers(couponId.toString(), count).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.COUPON_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("사용자에게 쿠폰을 지급한다")
    void issueCouponTest() {
        given(couponRepository.findById(any(ObjectId.class)))
                .willReturn(Mono.just(plusDayMockCoupon));
        given(couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId.toString()))
                .willReturn(Mono.just(mockCouponNumber));
        given(couponNumberRepository.save(any())).willReturn(Mono.just(mockCouponNumber));

        CouponNumber couponNumber = couponService.issueCoupon(couponId.toString(), userId).block();

        assertThat(couponNumber).isNotNull();
        assertThat(couponNumber.isUsersCouponNumber(userId)).isTrue();
    }

    @Test
    @DisplayName("사용자에게 쿠폰 지급 시 만료 된 쿠폰 일 경우 오류가 발생 한다")
    void issueCoupon_expiredCouponTest() {
        given(couponRepository.findById(any(ObjectId.class))).willReturn(Mono.just(minusDayMockCoupon));

        assertThatThrownBy(() -> couponService.issueCoupon(couponId.toString(), userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("사용자에게 쿠폰 지급 시 발급 가능한 번호가 없을 경우 오류가 발생 한다")
    void issueCoupon_notFoundIssueAbleNumberTest() {
        given(couponRepository.findById(any(ObjectId.class)))
                .willReturn(Mono.just(plusDayMockCoupon));
        given(couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId.toString()))
                .willReturn(Mono.empty());

        assertThatThrownBy(() -> couponService.issueCoupon(couponId.toString(), userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE);
    }

    @Test
    @DisplayName("사용자 ID로 쿠폰번호 목록을 가져온다")
    void getUserCouponsTest() {
        List<CouponNumber> mockCouponNumbers = Arrays.asList(
                new CouponNumber("11", couponId.toString()),
                new CouponNumber("22", couponId.toString())
        );
        given(couponNumberRepository.findAllByUserId(userId)).willReturn(Flux.fromIterable(mockCouponNumbers));

        List<CouponNumber> couponNumbers = couponService.getUserCoupons(userId).block();

        assertThat(couponNumbers).hasSize(mockCouponNumbers.size());
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용한다")
    void useCouponTest() {
        mockCouponNumber.issue(userId);
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.just(mockCouponNumber));
        given(couponRepository.findById(any(ObjectId.class))).willReturn(Mono.just(plusDayMockCoupon));
        given(couponNumberRepository.save(any())).willReturn(Mono.just(mockCouponNumber));

        CouponNumber couponNumber = couponService.useCoupon(number, userId).block();

        assertThat(couponNumber).isNotNull();
        assertThat(couponNumber.isUseFlag()).isTrue();
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 존재하는 쿠폰번호가 아닐 경우 오류가 발생한다")
    void useCoupon_notFoundCouponNumberTest() {
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.empty());

        assertThatThrownBy(() -> couponService.useCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.COUPON_NUMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 쿠폰 유효기간이 지났을 경우 오류가 발생한다")
    void useCoupon_expiredCouponTest() {
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.just(mockCouponNumber));
        given(couponRepository.findById(any(ObjectId.class))).willReturn(Mono.just(minusDayMockCoupon));

        assertThatThrownBy(() -> couponService.useCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 이미 사용한 쿠폰 일 경우 오류가 발생한다")
    void useCoupon_usedCouponNumberTest() {
        mockCouponNumber.issue(userId);
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.just(mockCouponNumber));
        given(couponRepository.findById(any(ObjectId.class))).willReturn(Mono.just(plusDayMockCoupon));
        given(couponNumberRepository.save(any())).willReturn(Mono.just(mockCouponNumber));

        couponService.useCoupon(number, userId).block();
        assertThatThrownBy(() -> couponService.useCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.IS_USED_COUPON_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 발급 받은 사용자가 아닐 경우 오류가 발생한다")
    void useCoupon_notMatchUserUseTest() {
        mockCouponNumber.issue(userId);
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.just(mockCouponNumber));
        given(couponRepository.findById(couponId)).willReturn(Mono.just(plusDayMockCoupon));

        assertThatThrownBy(() -> couponService.useCoupon(number, anotherUserId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.USER_NOT_MATCH_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 취소한다")
    void cancelCouponTest() {
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.just(mockUsedCouponNumber));
        given(couponNumberRepository.save(any())).willReturn(Mono.just(mockCouponNumber));

        CouponNumber couponNumber = couponService.cancelCoupon(number, userId).block();

        assertThat(couponNumber).isNotNull();
        assertThat(couponNumber.isUseFlag()).isFalse();
    }

    @Test
    @DisplayName("사용자 쿠폰을 취소시 존재하는 쿠폰번호가 아닐 경우 오류가 발생한다")
    void cancelCoupon_notFoundCouponNumberTest() {
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.empty());

        assertThatThrownBy(() -> couponService.cancelCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.COUPON_NUMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 취소시 이미 사용한 쿠폰 일 경우 오류가 발생한다")
    void cancelCoupon_usedCouponNumberTest() {
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.just(mockUsedCouponNumber));
        given(couponNumberRepository.save(any())).willReturn(Mono.just(mockCouponNumber));

        couponService.cancelCoupon(number, userId).block();
        assertThatThrownBy(() -> couponService.cancelCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.IS_UN_USED_COUPON_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 취소시 발급 받은 사용자가 아닐 경우 오류가 발생한다")
    void cancelCoupon_notMatchUserUseTest() {
        given(couponNumberRepository.findById(any(String.class))).willReturn(Mono.just(mockUsedCouponNumber));

        assertThatThrownBy(() -> couponService.cancelCoupon(number, anotherUserId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.USER_NOT_MATCH_MESSAGE);
    }

    @Test
    @DisplayName("기간내 만료 예정인 발급된 쿠폰번호를 조회한다")
    void getExpiredCouponNumbersBetweenDate() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now();
        List<CouponNumber> couponNumbers = setExpireCouponNumbers(start, end);

        List<CouponNumber> expireCouponNumbers = couponService.getExpiredCouponNumbersBetweenDate(start, end).block();

        assertThat(expireCouponNumbers).isNotNull();
        assertThat(expireCouponNumbers).hasSize(couponNumbers.size());
        expireCouponNumbers.forEach(couponNumber -> assertThat(couponNumber.getUserId()).isNotNull());
    }

    @Test
    @DisplayName("지정한 날짜에 만료 예정인 쿠폰 사용자에게 공지한다")
    void noticeExpiredCouponNumberBetweenDate() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now();
        setExpireCouponNumbers(start, end);

        couponService.noticeExpiredCouponNumberBetweenDate(start, end).block();
    }

    @Test
    @DisplayName("지난 날짜의 만료된 쿠폰 사용자에게 공지 할 경우 오류 발생")
    void noticeExpiredCouponNumberBetweenDate_beforDate() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> couponService.noticeExpiredCouponNumberBetweenDate(start, end).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.CAN_NOT_EXPIRE_NOTICE_BEFORE_DATE);
    }

    private List<CouponNumber> setExpireCouponNumbers(LocalDateTime start, LocalDateTime end) {
        Coupon coupon = new Coupon(couponId, price, LocalDateTime.now().minusHours(1));
        List<CouponNumber> couponNumbers = Arrays.asList(new CouponNumber("11", coupon.getId()), new CouponNumber("22", coupon.getId()));
        couponNumbers.forEach(couponNumber -> couponNumber.issue(userId));
        given(couponRepository.findAllByExpireDateTimeBetween(start, end))
                .willReturn(Flux.just(coupon));
        given(couponNumberRepository.findAllByCouponIdInAndUserIdNotNull(any()))
                .willReturn(Flux.fromIterable(couponNumbers));
        return couponNumbers;
    }
}
