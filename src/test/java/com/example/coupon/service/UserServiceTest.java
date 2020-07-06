package com.example.coupon.service;

import com.example.coupon.config.JwtTokenProvider;
import com.example.coupon.domain.User;
import com.example.coupon.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private UserService userService;
    private String jwtToken;
    private User user;
    private String username;
    private String password;

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        this.userService = new UserService(jwtTokenProvider, userRepository);
        this.jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1ZjAzNjI0YWQ2OTE2YTU4YTcxYTg0MGUiLCJuYW1lIjoic3RyaW5nIiwiaWF0IjoxNTk0MDU3NjEzLCJleHAiOjE1OTQwNjEyMTN9.lNRHg5gOYvCefBLsZcrDxxfuXDVBT2dIoN_QQkn9_rY";
        this.username = "tester";
        this.password = "1234";
        this.user = new User(username, password);
    }

    @Test
    @DisplayName("사용자 로그인 후 jwt 토큰을 제공한다")
    void signinTest() {
        given(userRepository.findFirstByUsernameAndPassword(any(), any()))
                .willReturn(Mono.just(user));
        given(jwtTokenProvider.createToken(user)).willReturn(jwtToken);

        String token = userService.signin(user).block();

        assertThat(token).isEqualTo(jwtToken);
    }

    @Test
    @DisplayName("사용자 로그인 시 정보를 찾을 수 없으면 오류가 발생한다")
    void signin_userNotFoundTest() {
        given(userRepository.findFirstByUsernameAndPassword(any(), any()))
                .willReturn(Mono.empty());

        assertThatThrownBy(() -> userService.signin(user).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(UserService.USER_INFO_NOT_FOUND_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("사용자 회원 가입 후 jwt 토큰을 제공한다")
    void signupTest() {
        given(userRepository.countByUsername(any()))
                .willReturn(Mono.just(0L));
        given(jwtTokenProvider.createToken(user)).willReturn(jwtToken);
        given(userRepository.save(user)).willReturn(Mono.just(user));

        String token = userService.signup(user).block();

        assertThat(token).isEqualTo(jwtToken);
    }

    @Test
    @DisplayName("사용자 회원 가입 시 중복된 이름을 가진 사용자가 있으면 오류가 발생한다")
    void signup_alreadyExistUserTest() {
        given(userRepository.countByUsername(any()))
                .willReturn(Mono.just(1L));

        assertThatThrownBy(() -> userService.signup(user).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(UserService.ALREADY_EXIST_USER_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("사용자를 조회 한다")
    void getUserInfoTest() {
        String userId = "5f03624ad6916a58a71a840e";
        User testUser = new User(new ObjectId(userId), username, password);
        given(userRepository.findById(any(ObjectId.class)))
                .willReturn(Mono.just(testUser));

        User user = userService.getUserInfo(userId).block();

        assertThat(user.getId()).isEqualTo(userId);
    }
}

