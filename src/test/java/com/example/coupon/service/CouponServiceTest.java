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

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponNumberRepository couponNumberRepository;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepository, couponNumberRepository);
    }

    @Test
    @DisplayName("쿠폰을 생성 한다")
    void createCouponTest() {
        long expectedId = 1;
        LocalDateTime expireDateTime = LocalDateTime.now().plusDays(3);
        long price = 1000;
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
        long couponId = 1;
        List<CouponNumber> mockCouponNumbers = Arrays.asList(new CouponNumber("11", couponId), new CouponNumber("22", couponId));
        Coupon mockCoupon = new Coupon(couponId, 1000, LocalDateTime.now().plusDays(3));
        given(couponRepository.findById(any())).willReturn(Optional.of(mockCoupon));
        given(couponNumberRepository.saveAll(any())).willReturn(mockCouponNumbers);

        List<CouponNumber> generatedCouponNumbers = couponService.generateCouponNumbers(couponId, count)
                .block();
        CouponNumbers couponNumbers = new CouponNumbers(generatedCouponNumbers);

        assertThat(couponNumbers.size()).isEqualTo(count);
        assertThat(couponNumbers.distinctNumbers()).hasSize(count);
    }

    @Test
    @DisplayName("쿠폰 번호 생성 시 만료 된 쿠폰일 경우 오류가 발생 한다")
    void generateCouponNumber_ExpiredCouponTest() {
        int count = 2;
        long couponId = 1;
        given(couponRepository.findById(any())).willReturn(Optional.of(new Coupon(couponId, 1000, LocalDateTime.now().minusDays(1))));

        assertThatThrownBy(() -> couponService.generateCouponNumbers(couponId, count).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("쿠폰 번호 생성 시 쿠폰 아이디를 찾을 수 없는 경우 오류가 발생 한다")
    void generateCouponNumber_NotFoundCouponTest() {
        int count = 2;
        long couponId = 1;
        given(couponRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.generateCouponNumbers(couponId, count).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.COUPON_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("사용자에게 쿠폰을 지급한다")
    void issueCouponTest() {
        long userId = 1;
        long couponId = 1;
        String number = "11";
        CouponNumber mockCouponNumber = new CouponNumber(number, couponId);
        given(couponRepository.findById(any()))
                .willReturn(Optional.of(new Coupon(couponId, 1000, LocalDateTime.now().plusDays(3))));
        given(couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId))
                .willReturn(Optional.of(mockCouponNumber));
        given(couponNumberRepository.save(any())).willReturn(mockCouponNumber);

        CouponNumber couponNumber = couponService.issueCoupon(couponId, userId).block();

        assertThat(couponNumber).isNotNull();
        assertThat(couponNumber.isUsersCouponNumber(userId)).isTrue();
    }

    @Test
    @DisplayName("사용자에게 쿠폰 지급 시 만료 된 쿠폰 일 경우 오류가 발생 한다")
    void issueCoupon_ExpiredCouponTest() {
        long userId = 1;
        long couponId = 1;
        given(couponRepository.findById(any())).willReturn(Optional.of(new Coupon(couponId, 1000, LocalDateTime.now().minusDays(1))));

        assertThatThrownBy(() -> couponService.issueCoupon(couponId, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Coupon.COUPON_EXPIRED_MESSAGE);
    }

    @Test
    @DisplayName("사용자에게 쿠폰 지급 시 발급 가능한 번호가 없을 경우 오류가 발생 한다")
    void issueCoupon_NotFoundIssueAbleNumberTest() {
        long userId = 1;
        long couponId = 1;
        given(couponRepository.findById(any()))
                .willReturn(Optional.of(new Coupon(couponId, 1000, LocalDateTime.now().plusDays(3))));
        given(couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.issueCoupon(couponId, userId).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CouponService.NOTFOUND_ISSUE_ABLE_COUPON_NUMBER_MESSAGE);
    }
}
