package com.example.coupon.controller;

import com.example.coupon.controller.dto.SigninRequest;
import com.example.coupon.controller.dto.SignupRequest;
import com.example.coupon.controller.dto.TokenResponse;
import com.example.coupon.domain.User;
import com.example.coupon.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signin")
    public Mono<TokenResponse> signIn(@RequestBody SigninRequest signinRequest) {
        return userService.signin(signinRequest.toUser())
                .map(TokenResponse::new);
    }

    @PostMapping("/signup")
    public Mono<TokenResponse> signup(@RequestBody SignupRequest signupRequest) {
        return userService.signup(signupRequest.toUser())
                .map(TokenResponse::new);
    }

    @GetMapping("/me")
    public Mono<User> currentUserInfo(Principal principal) {
        String userId = principal.getName();
        return userService.getUserInfo(userId);
    }
}
