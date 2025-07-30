package com.server.money_touch.domain.user.converter;

import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthConverter {

    public static User toUser(String email, String name, String password, PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(email)
                .role(Role.USER)
                .nickname(name)
                .build();
    }
}