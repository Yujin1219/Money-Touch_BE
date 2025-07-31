package com.server.money_touch.domain.user.converter;

import com.server.money_touch.domain.user.entity.User;
import com.server.money_touch.domain.user.enums.AuthType;
import com.server.money_touch.domain.user.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthConverter {

    public static User toUser(String email, String name, String password, PasswordEncoder passwordEncoder, Role role, AuthType authType) {
        return User.builder()
                .email(email)
                .role(role)
                .nickname(name)
                .authType(authType)
                .build();

    }
}