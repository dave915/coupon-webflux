package com.example.coupon.repository;

import com.example.coupon.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class UserRepositoryTest {
    private User user;
    private String username;
    private String password;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        this.username = "tester";
        this.password = "1234";
        this.user = new User(username, password);
    }

    @Test
    @DisplayName("유저 이름과 비밀번호로 유저를 조회한다")
    void findFirstByUsernameAndPasswordTest() {
        userRepository.save(user).block();

        User selectedUser = userRepository.findFirstByUsernameAndPassword(user.getUsername(), user.getPassword())
                .block();

        assertThat(selectedUser.getId()).isNotNull();
        assertThat(selectedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(selectedUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @DisplayName("유저 이름으로 count를 조회 한다")
    void countByUsername() {
        userRepository.save(user).block();

        long count = userRepository.countByUsername(username).block();

        assertThat(count).isNotZero();
    }
}
