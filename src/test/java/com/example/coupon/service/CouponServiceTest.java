package com.example.coupon.service;

import com.example.coupon.domain.Coupon;
import com.example.coupon.domain.CouponNumber;
import com.example.coupon.domain.CouponNumbers;
import com.example.coupon.repository.CouponNumberRepository;
import com.example.coupon.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {
    private CouponService couponService;
    private long userId;
    private long price;
    private long couponId;
    private String number;
    private Coupon plusDayMockCoupon;
    private Coupon minusDayMockCoupon;
    private CouponNumber mockCouponNumber;

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponNumberRepository couponNumberRepository;

    @BeforeEach
    void setUp() {
        this.couponService = new CouponService(couponRepository, couponNumberRepository);
        this.userId = 1;
        this.price = 1000;
        this.couponId = 1;
        this.number = "11";
        this.plusDayMockCoupon = new Coupon(couponId, 1000, LocalDateTime.now().plusDays(3));
        this.minusDayMockCoupon = new Coupon(couponId, 1000, LocalDateTime.now().minusDays(3));
        this.mockCouponNumber = new CouponNumber(number, couponId);
    }

    @Test
    @DisplayName("쿠폰을 생성 한다")
    void createCouponTest() {
        long expectedId = 1;
        LocalDateTime expireDateTime = LocalDateTime.now().plusDays(3);
        Coupon coupon = new Coupon(price, expireDateTime);
        given(couponRepository.save(any())).willReturn(new Coupon(expectedId, price, expireDateTime));

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
        List<CouponNumber> mockCouponNumbers = Arrays.asList(new CouponNumber("11", couponId), new CouponNumber("22", couponId));
        given(couponRepository.findById(any())).willReturn(Optional.of(plusDayMockCoupon));
        given(couponNumberRepository.saveAll(any())).willReturn(mockCouponNumbers);

        List<CouponNumber> generatedCouponNumbers = couponService.generateCouponNumbers(couponId, count)
                .block();
        CouponNumbers couponNumbers = new CouponNumbers(generatedCouponNumbers);

        assertThat(couponNumbers.size()).isEqualTo(count);
        assertThat(couponNumbers.distinctNumbers()).hasSize(count);
    }

    @Test
    @DisplayName("쿠폰 번호 생성 시 만료 된 쿠폰일 경우 오류가 발생 한다")
    void generateCouponNumber_expiredCouponTest() {
        int count = 2;
        given(couponRepository.findById(any())).willReturn(Optional.of(minusDayMockCoupon));

        assertThatThrownBy(() -> couponService.generateCouponNumbers(couponId, count).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("쿠폰 번호 생성 시 쿠폰 아이디를 찾을 수 없는 경우 오류가 발생 한다")
    void generateCouponNumber_notFoundCouponTest() {
        int count = 2;
        given(couponRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.generateCouponNumbers(couponId, count).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.COUPON_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("사용자에게 쿠폰을 지급한다")
    void issueCouponTest() {
        given(couponRepository.findById(any()))
                .willReturn(Optional.of(plusDayMockCoupon));
        given(couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId))
                .willReturn(Optional.of(mockCouponNumber));
        given(couponNumberRepository.save(any())).willReturn(mockCouponNumber);

        CouponNumber couponNumber = couponService.issueCoupon(couponId, userId).block();

        assertThat(couponNumber).isNotNull();
        assertThat(couponNumber.isUsersCouponNumber(userId)).isTrue();
    }

    @Test
    @DisplayName("사용자에게 쿠폰 지급 시 만료 된 쿠폰 일 경우 오류가 발생 한다")
    void issueCoupon_expiredCouponTest() {
        given(couponRepository.findById(any())).willReturn(Optional.of(minusDayMockCoupon));

        assertThatThrownBy(() -> couponService.issueCoupon(couponId, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("사용자에게 쿠폰 지급 시 발급 가능한 번호가 없을 경우 오류가 발생 한다")
    void issueCoupon_notFoundIssueAbleNumberTest() {
        given(couponRepository.findById(any()))
                .willReturn(Optional.of(plusDayMockCoupon));
        given(couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.issueCoupon(couponId, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE);
    }

    @Test
    @DisplayName("사용자 ID로 쿠폰번호 목록을 가져온다")
    void getUserCouponsTest() {
        List<CouponNumber> mockCouponNumbers = Arrays.asList(
                new CouponNumber("11", couponId),
                new CouponNumber("22", couponId)
        );
        given(couponNumberRepository.findAllByUserId(userId)).willReturn(mockCouponNumbers);

        List<CouponNumber> couponNumbers = couponService.getUserCoupons(userId).block();

        assertThat(couponNumbers).hasSize(mockCouponNumbers.size());
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용한다")
    void useCouponTest() {
        mockCouponNumber.issue(userId);
        given(couponNumberRepository.findById(any())).willReturn(Optional.of(mockCouponNumber));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(plusDayMockCoupon));
        given(couponNumberRepository.save(any())).willReturn(mockCouponNumber);

        CouponNumber couponNumber = couponService.useCoupon(number, userId).block();

        assertThat(couponNumber).isNotNull();
        assertThat(couponNumber.isUse()).isTrue();
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 존재하는 쿠폰번호가 아닐 경우 오류가 발생한다")
    void useCoupon_notFoundCouponNumberTest() {
        given(couponNumberRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.useCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.COUPON_NUMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 쿠폰 유효기간이 지났을 경우 오류가 발생한다")
    void useCoupon_expiredCouponTest() {
        given(couponNumberRepository.findById(any())).willReturn(Optional.of(mockCouponNumber));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(minusDayMockCoupon));

        assertThatThrownBy(() -> couponService.useCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 이미 사용한 쿠폰 일 경우 오류가 발생한다")
    void useCoupon_usedCouponNumberTest() {
        mockCouponNumber.issue(userId);
        given(couponNumberRepository.findById(any())).willReturn(Optional.of(mockCouponNumber));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(plusDayMockCoupon));
        given(couponNumberRepository.save(any())).willReturn(mockCouponNumber);

        couponService.useCoupon(number, userId).block();
        assertThatThrownBy(() -> couponService.useCoupon(number, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.IS_USED_COUPON_MESSAGE);
    }

    @Test
    @DisplayName("사용자 쿠폰을 사용시 발급 받은 사용자가 아닐 경우 오류가 발생한다")
    void useCoupon_notMatchUserUseTest() {
        mockCouponNumber.issue(userId);
        given(couponNumberRepository.findById(any())).willReturn(Optional.of(mockCouponNumber));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(plusDayMockCoupon));

        assertThatThrownBy(() -> couponService.useCoupon(number, 0).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponNumber.USER_NOT_MATCH_MESSAGE);
    }
}
