package com.example.coupon.controller.dto;

import com.example.coupon.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SigninRequest {
    private String username;
    private String password;

    public User toUser() {
        return new User(username, password);
    }
}
