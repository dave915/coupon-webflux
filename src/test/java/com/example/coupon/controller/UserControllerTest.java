package com.example.coupon.controller;

import com.example.coupon.config.JwtTokenProvider;
import com.example.coupon.controller.dto.SigninRequest;
import com.example.coupon.domain.User;
import com.example.coupon.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(JwtTokenProvider.class)
public class UserControllerTest {
    private String expiredJwtToken;

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        this.expiredJwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1ZjAzNjI0YWQ2OTE2YTU4YTcxYTg0MGUiLCJuYW1lIjoic3RyaW5nIiwiaWF0IjoxNTk0MDU3NjEzLCJleHAiOjE1OTQwNjEyMTN9.lNRHg5gOYvCefBLsZcrDxxfuXDVBT2dIoN_QQkn9_rY";
    }

    @Test
    @DisplayName("회원가입 후 토큰을 반환")
    void signingTest() {
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setUsername("tester");
        signinRequest.setPassword("1234");

        given(userService.signin(any())).willReturn(Mono.just(expiredJwtToken));

        webTestClient.post()
                .uri("/users/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(signinRequest)
                .exchange()
                .expectBody()
                .jsonPath("$.token").isEqualTo(expiredJwtToken);
    }

    @Test
    @DisplayName("현재 유저 정보를 조회 한다")
    void currentUserInfoTest() {
        String userId = "5f03624ad6916a58a71a840e";
        User user = new User(new ObjectId(userId), "tester", "1234");
        String jwtToken = jwtTokenProvider.createToken(user);

        given(userService.getUserInfo(any())).willReturn(Mono.just(user));

        webTestClient.get()
                .uri("/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId);
    }

    @Test
    @DisplayName("authorization 헤더가 없으면 401에러가 발생한다")
    void currentUserInfo_emptyTokenTest() {
        webTestClient.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("jwt 토큰이 만료되었으면 401에러가 발생한다")
    void currentUserInfo_expiredTokenTest() {
        webTestClient.get()
                .uri("/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredJwtToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
