package com.example.coupon.repository;

import com.example.coupon.domain.CouponNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@ExtendWith(SpringExtension.class)
public class CouponNumberRepositoryTest {
    long userId;

    @Autowired
    private CouponNumberRepository couponNumberRepository;

    @BeforeEach
    void setUp() {
        this.userId = 3;
    }

    @Test
    @DisplayName("Batch insert를 테스트 한다.")
    void saveAllTest() {
        List<CouponNumber> couponNumbers = Arrays.asList(
                new CouponNumber("11", 1),
                new CouponNumber("22", 1),
                new CouponNumber("33", 1)
        );

        Iterable<CouponNumber> insertedCouponNumbers = couponNumberRepository.saveAll(couponNumbers);

        assertThat(insertedCouponNumbers).hasSize(couponNumbers.size());
    }

    @ParameterizedTest()
    @MethodSource(value = "findFirstByCouponIdAndUserIdNullSource")
    @DisplayName("발급되지 않은 쿠폰번호를 조회 한다")
    void findFirstByCouponIdAndUserIdNullTest(long couponId, List<CouponNumber> couponNumbers, boolean isPresent) {
        couponNumberRepository.saveAll(couponNumbers);
        Optional<CouponNumber> optionalCouponNumber = couponNumberRepository.findFirstByCouponIdAndUserIdNull(couponId);

        assertThat(optionalCouponNumber.isPresent()).isEqualTo(isPresent);
    }

    static Stream<Arguments> findFirstByCouponIdAndUserIdNullSource() {
        long couponId = 1;
        List<CouponNumber> notIssuedCouponNumbers = Arrays.asList(
                new CouponNumber("11", couponId),
                new CouponNumber("22", couponId)
        );
        List<CouponNumber> allIssuedCouponNumbers = Arrays.asList(
                new CouponNumber("11", couponId),
                new CouponNumber("22", couponId)
        );
        allIssuedCouponNumbers.forEach(couponNumber -> couponNumber.issue(3));

        return Stream.of(
                Arguments.of(couponId, notIssuedCouponNumbers, true),
                Arguments.of(couponId, allIssuedCouponNumbers, false)
        );
    }

    @Test
    @DisplayName("유저의 쿠폰 번호들을 조회한다.")
    void findAllByUserIdTest() {
        List<CouponNumber> couponNumbers = getIssuedCouponNumbers(1);

        couponNumberRepository.saveAll(couponNumbers);
        List<CouponNumber> usersCouponNumber = couponNumberRepository.findAllByUserId(userId);

        assertThat(usersCouponNumber).hasSize(couponNumbers.size() - 1);
    }

    @Test
    void findAllByCouponIdInAndUserIdNotNullTest() {
        List<CouponNumber> issuedCouponNumbers = getIssuedCouponNumbers(0);
        List<CouponNumber> unIssuedCouponNumbers = Arrays.asList(
                new CouponNumber("11", 1),
                new CouponNumber("22", 2),
                new CouponNumber("33", 3)
        );
        List<CouponNumber> testCouponNumbers = new ArrayList<>();
        testCouponNumbers.addAll(issuedCouponNumbers);
        testCouponNumbers.addAll(unIssuedCouponNumbers);
        List<Long> searchCouponIds = Arrays.asList(1L, 2L);

        couponNumberRepository.saveAll(testCouponNumbers);
        List<CouponNumber> couponNumbers = couponNumberRepository.findAllByCouponIdInAndUserIdNotNull(searchCouponIds);

        assertThat(couponNumbers).hasSize(2);
        couponNumbers.forEach(couponNumber -> assertThat(couponNumber.getUserId()).isEqualTo(userId));
    }

    private List<CouponNumber> getIssuedCouponNumbers(int startIndex) {
        List<CouponNumber> couponNumbers = Arrays.asList(
                new CouponNumber("11", 1),
                new CouponNumber("22", 2),
                new CouponNumber("33", 3)
        );
        IntStream.range(startIndex, couponNumbers.size())
                .forEach(i -> couponNumbers.get(i).issue(userId));
        return couponNumbers;
    }
}
